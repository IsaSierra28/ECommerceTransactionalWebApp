package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.Payment;

import java.math.BigDecimal;

/**
 * CASE 2 - Service interface + multiple implementations.
 *
 * There are two ways to pay in our store, and each one has different business rules:
 *   - CreditCardPaymentServiceImpl   -> checks the card data (number, expiry date, CVV) and a card limit
 *   - BankTransferPaymentServiceImpl -> checks the bank data (institution, transit and account number)
 *
 * OrderService only knows this interface. Spring injects both implementations
 * with @Qualifier, and OrderService chooses the right one with the payment method of the order.
 * If we add a new payment method later (for example PayPal), we only add a new implementation.
 */
public interface PaymentService {

    /**
     * Validates the payment information and creates the Payment for the order.
     * If something is wrong it throws an exception, so the whole purchase is rolled back.
     *
     * @param order       the order that is being paid
     * @param amountDue   the total of the order
     * @param request     the payment information sent by the customer
     * @return the Payment to be saved (not saved yet)
     */
    Payment processPayment(CustomerOrder order, BigDecimal amountDue, PaymentRequest request);
}
