package com.example.ecommerce.dto;

public record VendorResponse(
        Integer id,
        String username,
        String email,
        String companyName,
        String phone,
        Boolean active
) {
}
