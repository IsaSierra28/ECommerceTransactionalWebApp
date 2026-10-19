package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Integer id,
        String paymentMethod,
        BigDecimal amount,
        LocalDateTime paymentDate
) {
}
