package com.example.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must have at most 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must have at most 50 characters")
        String lastName,

        @Size(max = 20, message = "Phone must have at most 20 characters")
        String phone
) {
}
