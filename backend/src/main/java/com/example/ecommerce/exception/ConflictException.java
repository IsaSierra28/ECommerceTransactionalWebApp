package com.example.ecommerce.exception;

/**
 * Thrown when the operation is not possible because of the current data,
 * for example a duplicated username or deleting a product that was already ordered (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
