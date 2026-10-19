package com.example.ecommerce.exception;

/**
 * Thrown when the payment amount does not cover the order total (HTTP 409).
 * It is a RuntimeException, so @Transactional makes a rollback.
 */
public class InsufficientPaymentException extends RuntimeException {

    public InsufficientPaymentException(String message) {
        super(message);
    }
}
