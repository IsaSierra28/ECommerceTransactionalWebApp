package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ErrorResponse;
import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "The purchase transaction (COMMIT or ROLLBACK) and the list of orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place an order (purchase transaction)",
            description = "CUSTOMER only. Creates the order, the order items, updates the inventory, creates the invoice "
                    + "and records the payment in ONE transaction. If something fails, everything is rolled back.\n\n"
                    + "Test credit card: 4111 1111 1111 1111, expiry 12/30, CVV 123.\n\n"
                    + "Test bank account: institution 001, transit 12345, account 1234567.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "COMMIT - order created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or invalid payment data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not logged in",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Only customers can buy",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ROLLBACK - insufficient inventory, insufficient payment "
                    + "or the product was bought by another customer at the same time",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request, Principal principal) {
        OrderResponse order = orderService.placeOrder(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    @Operation(summary = "List orders",
            description = "ADMIN sees all orders, VENDOR sees the orders with their products, CUSTOMER sees their own orders.")
    public List<OrderResponse> getOrders(Principal principal) {
        return orderService.findOrders(principal.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "403", description = "The order belongs to another customer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse getOrder(@PathVariable Integer id, Principal principal) {
        return orderService.findOrder(id, principal.getName());
    }
}
