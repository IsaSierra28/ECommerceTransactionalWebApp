package com.example.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data to create a new vendor. It also creates the user account of the vendor.
 */
public record VendorRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must have between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must have at least 8 characters")
        String password,

        @NotBlank(message = "Company name is required")
        @Size(max = 100, message = "Company name must have at most 100 characters")
        String companyName,

        @Size(max = 20, message = "Phone must have at most 20 characters")
        String phone
) {
}
