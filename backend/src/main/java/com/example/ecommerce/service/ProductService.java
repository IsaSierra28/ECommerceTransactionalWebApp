package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ConflictException;
import com.example.ecommerce.exception.ForbiddenOperationException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Vendor;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CASE 1 - Service only.
 *
 * ProductService is a concrete @Service class, without an interface.
 * There is only one way to manage products in our store, so a separate
 * interface + implementation would add files without any benefit.
 *
 * ProductController -> ProductService -> ProductRepository
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          VendorRepository vendorRepository,
                          OrderItemRepository orderItemRepository,
                          UserService userService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(Integer categoryId) {
        List<Product> products = (categoryId == null)
                ? productRepository.findAll()
                : productRepository.findByCategoryId(categoryId);
        return products.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Integer id) {
        return toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String username) {
        Vendor vendor = resolveVendor(request, username);

        if (!vendor.getActive()) {
            throw new BadRequestException("The vendor '" + vendor.getCompanyName() + "' is inactive and cannot add products");
        }
        if (productRepository.existsBySku(request.sku())) {
            throw new ConflictException("A product with SKU '" + request.sku() + "' already exists");
        }

        Product product = new Product();
        product.setVendor(vendor);
        copyData(request, product);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request, String username) {
        Product product = getProduct(id);
        checkOwnership(product, username);

        if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
            throw new ConflictException("A product with SKU '" + request.sku() + "' already exists");
        }

        // Only an ADMIN can move a product to another vendor
        if (request.vendorId() != null && UserService.ADMIN.equals(userService.getRole(username))) {
            product.setVendor(getVendor(request.vendorId()));
        }
        copyData(request, product);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Integer id, String username) {
        Product product = getProduct(id);
        checkOwnership(product, username);

        // Old orders must keep their products, so a product that was sold cannot be deleted
        if (orderItemRepository.existsByProductId(id)) {
            throw new ConflictException("The product cannot be deleted because it is part of existing orders. " +
                    "You can set its quantity to 0 instead.");
        }
        productRepository.delete(product);
    }

    // ---------- helper methods ----------

    private void copyData(ProductRequest request, Product product) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category " + request.categoryId() + " not found"));

        product.setCategory(category);
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        // Same rule as the SQL Server trigger, so the response already shows the correct status
        product.setStatus(request.quantity() == 0 ? "OUT_OF_STOCK" : "IN_STOCK");
    }

    /**
     * A VENDOR always works with their own company.
     * An ADMIN must say for which vendor the product is created.
     */
    private Vendor resolveVendor(ProductRequest request, String username) {
        String role = userService.getRole(username);

        if (UserService.VENDOR.equals(role)) {
            return vendorRepository.findByUserUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for user " + username));
        }
        if (UserService.ADMIN.equals(role)) {
            if (request.vendorId() == null) {
                throw new BadRequestException("An admin must send the vendorId of the product");
            }
            return getVendor(request.vendorId());
        }
        throw new ForbiddenOperationException("Only admins and vendors can manage products");
    }

    /**
     * A VENDOR can only change their own products. An ADMIN can change any product.
     */
    private void checkOwnership(Product product, String username) {
        String role = userService.getRole(username);
        if (UserService.ADMIN.equals(role)) {
            return;
        }
        if (UserService.VENDOR.equals(role)
                && product.getVendor().getUser().getUsername().equals(username)) {
            return;
        }
        throw new ForbiddenOperationException("You can only manage your own products");
    }

    private Product getProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    }

    private Vendor getVendor(Integer id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor " + id + " not found"));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getStatus(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getVendor().getId(),
                product.getVendor().getCompanyName());
    }
}
