package com.example.ecommerce.dto;

public record AddressResponse(
        Integer id,
        String street,
        String city,
        String province,
        String postalCode,
        String country
) {
}
