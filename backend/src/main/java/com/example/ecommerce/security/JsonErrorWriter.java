package com.example.ecommerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Writes a 401 or 403 error with the same JSON format as GlobalExceptionHandler.
 * These two errors happen in the security filters, before the controllers,
 * so they cannot be handled by @RestControllerAdvice.
 */
final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void write(HttpServletRequest request, HttpServletResponse response,
                      int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + error + "\","
                + "\"message\":\"" + message + "\","
                + "\"path\":\"" + request.getRequestURI() + "\","
                + "\"fieldErrors\":null"
                + "}";
        response.getWriter().write(json);
    }
}
