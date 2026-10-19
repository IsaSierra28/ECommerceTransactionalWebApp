package com.example.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(

        @NotNull(message = "Shipping address is required")
        Integer addressId,

        @NotEmpty(message = "The order must have at least one item")
        List<@Valid OrderItemRequest> items,

        @NotNull(message = "Payment information is required")
        @Valid
        PaymentRequest payment
) {
}
