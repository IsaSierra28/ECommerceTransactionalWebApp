package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ErrorResponse;
import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * The controller only handles HTTP: it receives the request, calls the service and returns the response.
 * All the business rules are in ProductService.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "View products (public) and manage products (ADMIN, VENDOR)")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Public. Optional filter by category.")
    public List<ProductResponse> getProducts(
            @Parameter(description = "Category id to filter the products") @RequestParam(required = false) Integer categoryId) {
        return productService.findAll(categoryId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one product", description = "Public.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse getProduct(@PathVariable Integer id) {
        return productService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a product",
            description = "ADMIN or VENDOR. A vendor creates products for their own company; an admin must send vendorId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not logged in",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Role not allowed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "SKU already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request,
                                                         Principal principal) {
        ProductResponse created = productService.createProduct(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "ADMIN, or the VENDOR that owns the product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the owner of the product",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse updateProduct(@PathVariable Integer id,
                                         @Valid @RequestBody ProductRequest request,
                                         Principal principal) {
        return productService.updateProduct(id, request, principal.getName());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product",
            description = "ADMIN, or the VENDOR that owns the product. A product that is part of orders cannot be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "The product is part of existing orders",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id, Principal principal) {
        productService.deleteProduct(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
