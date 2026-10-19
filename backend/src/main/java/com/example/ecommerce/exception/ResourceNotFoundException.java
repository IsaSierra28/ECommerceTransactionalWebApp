package com.example.ecommerce.exception;

/**
 * Thrown when something that the client asked for does not exist (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
