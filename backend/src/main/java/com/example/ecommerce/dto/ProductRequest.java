package com.example.ecommerce.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Data sent by the client to create or update a product.
 * vendorId is only used by an ADMIN. A VENDOR always creates products for their own company.
 */
public record ProductRequest(

        Integer vendorId,

        @NotNull(message = "Category is required")
        Integer categoryId,

        @NotBlank(message = "SKU is required")
        @Size(max = 30, message = "SKU must have at most 30 characters")
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must have at most 100 characters")
        String name,

        @Size(max = 500, message = "Description must have at most 500 characters")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price can have at most 2 decimals")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be 0 or more")
        Integer quantity
) {
}
