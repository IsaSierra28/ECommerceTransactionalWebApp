package com.example.ecommerce.dto;

public record CustomerResponse(
        Integer id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone
) {
}
