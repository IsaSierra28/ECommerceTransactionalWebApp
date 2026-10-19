package com.example.ecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI documentation.
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * Click "Authorize" and log in with a test user (for example maria / Password123!).
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce Transactional Web Application - REST API",
                version = "1.0",
                description = "REST API of the online store: products, vendors, customers, orders, invoices and payments. "
                        + "Test users (password Password123!): admin (ADMIN), techstore (VENDOR), maria (CUSTOMER)."),
        security = @SecurityRequirement(name = "basicAuth"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
