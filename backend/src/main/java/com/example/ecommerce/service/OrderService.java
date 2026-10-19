package com.example.ecommerce.service;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ForbiddenOperationException;
import com.example.ecommerce.exception.InsufficientInventoryException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The purchase transaction.
 *
 * placeOrder() is annotated with @Transactional, so all the database changes
 * (order, order items, inventory, invoice and payment) belong to ONE transaction:
 *   - if every step works            -> COMMIT  (everything is saved)
 *   - if any step throws an exception -> ROLLBACK (nothing is saved, no partial order)
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final VendorRepository vendorRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    // CASE 2: the two implementations of PaymentService are injected with @Qualifier
    private final PaymentService creditCardPaymentService;
    private final PaymentService bankTransferPaymentService;

    public OrderService(CustomerOrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        AddressRepository addressRepository,
                        VendorRepository vendorRepository,
                        InvoiceRepository invoiceRepository,
                        PaymentRepository paymentRepository,
                        UserService userService,
                        @Qualifier("creditCardPaymentService") PaymentService creditCardPaymentService,
                        @Qualifier("bankTransferPaymentService") PaymentService bankTransferPaymentService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.vendorRepository = vendorRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.creditCardPaymentService = creditCardPaymentService;
        this.bankTransferPaymentService = bankTransferPaymentService;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String username) {
        log.info("=== START purchase transaction for user {} ===", username);

        // 1. Verify customer
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ForbiddenOperationException("Only customers can place orders"));

        // 2. Verify the shipping address (it must belong to the same customer)
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address " + request.addressId() + " not found"));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("The shipping address does not belong to the customer");
        }

        // If the same product is sent twice, we join the quantities in one line
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest item : request.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        // 3. Verify products and 4. calculate the order total
        List<Product> products = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product " + entry.getKey() + " not found"));
            if (!product.getVendor().getActive()) {
                throw new BadRequestException("The product '" + product.getName() + "' cannot be bought because its vendor is inactive");
            }
            products.add(product);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        log.info("Order total calculated: {}", total);

        // 5. Create the order
        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setStatus("PAID");
        order.setTotal(total);
        orderRepository.save(order);
        log.info("Order #{} inserted", order.getId());

        // 6. Check inventory, create the order items and update the inventory
        for (Product product : products) {
            int quantity = quantities.get(product.getId());

            if (product.getQuantity() < quantity) {
                // Exception -> the transaction is rolled back, so the order inserted above is removed too
                throw new InsufficientInventoryException("Insufficient inventory for '" + product.getName()
                        + "': requested " + quantity + ", available " + product.getQuantity());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());   // price at the moment of the purchase
            orderItemRepository.save(item);

            // The @Version column of Product protects this update against two purchases at the same time
            product.setQuantity(product.getQuantity() - quantity);
            product.setStatus(product.getQuantity() == 0 ? "OUT_OF_STOCK" : "IN_STOCK");
            productRepository.save(product);
            log.info("Inventory of '{}' updated: {} left", product.getName(), product.getQuantity());
        }

        // 7. Create the invoice
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + Year.now() + "-" + String.format("%05d", order.getId()));
        invoice.setTotal(total);
        invoiceRepository.save(invoice);
        log.info("Invoice {} created", invoice.getInvoiceNumber());

        // 8. Process the payment with the selected implementation and record it
        PaymentService paymentService = selectPaymentService(request.payment().method());
        Payment payment = paymentService.processPayment(order, total, request.payment());
        paymentRepository.save(payment);
        log.info("Payment recorded");

        log.info("=== COMMIT purchase transaction: order #{} ===", order.getId());
        return toResponse(order);
    }

    /**
     * Spring gives us both implementations. Here we choose which one to use
     * with the payment method selected by the customer.
     */
    private PaymentService selectPaymentService(String method) {
        return switch (method) {
            case "CREDIT_CARD" -> creditCardPaymentService;
            case "BANK_TRANSFER" -> bankTransferPaymentService;
            default -> throw new BadRequestException("Payment method " + method + " is not supported");
        };
    }

    /**
     * ADMIN sees all orders, VENDOR sees the orders with their products, CUSTOMER sees only their orders.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrders(String username) {
        String role = userService.getRole(username);
        List<CustomerOrder> orders = switch (role) {
            case UserService.ADMIN -> orderRepository.findAllByOrderByOrderDateDesc();
            case UserService.VENDOR -> orderRepository.findOrdersByVendorId(getVendorId(username));
            default -> orderRepository.findByCustomerIdOrderByOrderDateDesc(getCustomerId(username));
        };
        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrder(Integer id, String username) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));

        String role = userService.getRole(username);
        boolean allowed = switch (role) {
            case UserService.ADMIN -> true;
            case UserService.VENDOR -> orderItemRepository.findByOrderId(id).stream()
                    .anyMatch(i -> i.getProduct().getVendor().getId().equals(getVendorId(username)));
            default -> order.getCustomer().getId().equals(getCustomerId(username));
        };
        if (!allowed) {
            throw new ForbiddenOperationException("You cannot see this order");
        }
        return toResponse(order);
    }

    private Integer getCustomerId(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"))
                .getId();
    }

    private Integer getVendorId(String username) {
        return vendorRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"))
                .getId();
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();

        InvoiceResponse invoice = invoiceRepository.findByOrderId(order.getId())
                .map(InvoiceService::toResponse)
                .orElse(null);

        List<PaymentResponse> payments = paymentRepository.findByOrderId(order.getId()).stream()
                .map(p -> new PaymentResponse(p.getId(), p.getPaymentMethod(), p.getAmount(), p.getPaymentDate()))
                .toList();

        Customer customer = order.getCustomer();
        Address a = order.getAddress();
        String shippingAddress = a.getStreet() + ", " + a.getCity() + ", " + a.getProvince() + " "
                + a.getPostalCode() + ", " + a.getCountry();

        return new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotal(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                shippingAddress,
                items,
                invoice,
                payments);
    }
}
