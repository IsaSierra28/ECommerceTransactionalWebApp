package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String status,
        Integer categoryId,
        String categoryName,
        Integer vendorId,
        String vendorName
) {
}
