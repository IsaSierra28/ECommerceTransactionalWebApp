package com.example.ecommerce.dto;

public record UserInfoResponse(
        Integer userId,
        String username,
        String email,
        String role
) {
}
