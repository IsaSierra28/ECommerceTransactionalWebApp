package com.example.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer id,
        LocalDateTime orderDate,
        String status,
        BigDecimal total,
        Integer customerId,
        String customerName,
        String shippingAddress,
        List<OrderItemResponse> items,
        InvoiceResponse invoice,
        List<PaymentResponse> payments
) {
}
