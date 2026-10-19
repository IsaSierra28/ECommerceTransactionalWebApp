package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Integer id,
        String invoiceNumber,
        LocalDateTime issueDate,
        BigDecimal total,
        Integer orderId,
        String customerName
) {
}
