package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddressResponse;
import com.example.ecommerce.dto.CustomerRequest;
import com.example.ecommerce.dto.CustomerResponse;
import com.example.ecommerce.dto.CustomerUpdateRequest;
import com.example.ecommerce.dto.ErrorResponse;
import com.example.ecommerce.service.CustomerService;
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
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Manage customers (ADMIN) and see own profile (CUSTOMER)")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get my customer profile", description = "CUSTOMER only.")
    public CustomerResponse getMyProfile(Principal principal) {
        return customerService.findMyProfile(principal.getName());
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get my shipping addresses", description = "CUSTOMER only. Used to choose the address of an order.")
    public List<AddressResponse> getMyAddresses(Principal principal) {
        return customerService.findMyAddresses(principal.getName());
    }

    @GetMapping
    @Operation(summary = "List all customers", description = "ADMIN only.")
    public List<CustomerResponse> getCustomers() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one customer", description = "ADMIN only.")
    @ApiResponse(responseCode = "404", description = "Customer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CustomerResponse getCustomer(@PathVariable Integer id) {
        return customerService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a customer", description = "ADMIN only. Also creates the user account (role CUSTOMER).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer", description = "ADMIN only.")
    public CustomerResponse updateCustomer(@PathVariable Integer id, @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer", description = "ADMIN only. A customer with orders cannot be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Customer deleted"),
            @ApiResponse(responseCode = "409", description = "The customer has orders",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
