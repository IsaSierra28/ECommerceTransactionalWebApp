package com.example.ecommerce.exception;

/**
 * Thrown when a logged user tries to change data that is not theirs,
 * for example a vendor editing a product of another vendor (HTTP 403).
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
