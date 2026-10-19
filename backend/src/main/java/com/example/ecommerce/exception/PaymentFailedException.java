package com.example.ecommerce.exception;

/**
 * Thrown when the payment information is not valid, for example
 * an expired credit card or a wrong bank account number (HTTP 400).
 */
public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }
}
