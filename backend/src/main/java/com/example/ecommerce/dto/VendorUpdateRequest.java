package com.example.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VendorUpdateRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,

        @NotBlank(message = "Company name is required")
        @Size(max = 100, message = "Company name must have at most 100 characters")
        String companyName,

        @Size(max = 20, message = "Phone must have at most 20 characters")
        String phone
) {
}
