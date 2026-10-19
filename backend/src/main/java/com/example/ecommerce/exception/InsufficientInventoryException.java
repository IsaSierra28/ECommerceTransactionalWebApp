package com.example.ecommerce.exception;

/**
 * Thrown when a customer wants to buy more units than the stock (HTTP 409).
 * It is a RuntimeException, so @Transactional makes a rollback.
 */
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String message) {
        super(message);
    }
}
