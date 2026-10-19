package com.example.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The same JSON format is returned for every error of the API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
