package com.example.ecommerce.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Payment information sent with the order.
 * Credit card fields are used when method = CREDIT_CARD.
 * Bank fields are used when method = BANK_TRANSFER.
 * Card and bank numbers are only checked, they are never saved in the database.
 */
public record PaymentRequest(

        @NotBlank(message = "Payment method is required")
        @Pattern(regexp = "CREDIT_CARD|BANK_TRANSFER", message = "Payment method must be CREDIT_CARD or BANK_TRANSFER")
        String method,

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Payment amount can have at most 2 decimals")
        BigDecimal amount,

        // Credit card
        String cardHolder,
        String cardNumber,
        String expiryDate,
        String cvv,

        // Bank transfer
        String accountHolder,
        String institutionNumber,
        String transitNumber,
        String accountNumber
) {
}
