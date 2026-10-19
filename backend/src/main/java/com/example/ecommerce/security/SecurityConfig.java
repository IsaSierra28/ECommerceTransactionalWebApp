package com.example.ecommerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 *
 * - Authentication: HTTP Basic (username + password in every request), checked against the app_user table.
 * - Passwords: BCrypt (never saved as plain text).
 * - Authorization: each endpoint is allowed only for some roles. This is enforced in the BACKEND,
 *   so hiding a button in React is not enough to do a forbidden operation.
 * - CORS: only the React app (http://localhost:5173) can call the API from the browser.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // A REST API with HTTP Basic does not use sessions or cookies, so CSRF protection is not needed
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger documentation
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Everybody can see the products and categories
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()

                        // Products: create, update and delete -> ADMIN or VENDOR (vendors only their own products)
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "VENDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "VENDOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("ADMIN", "VENDOR")

                        // Vendors -> only ADMIN
                        .requestMatchers("/api/vendors/**").hasRole("ADMIN")

                        // Customers: a customer sees their own profile, the ADMIN manages all customers
                        .requestMatchers("/api/customers/me/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/customers/**").hasRole("ADMIN")

                        // Orders: only customers can buy
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "VENDOR", "CUSTOMER")

                        // Invoices: ADMIN and CUSTOMER (customers only their own invoices)
                        .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers("/api/auth/**").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, ex) ->
                        JsonErrorWriter.write(request, response, 401, "Unauthorized",
                                "You must log in with a valid username and password")))
                .exceptionHandling(ex -> ex
                        // 401: no login or wrong password
                        .authenticationEntryPoint((request, response, e) ->
                                JsonErrorWriter.write(request, response, 401, "Unauthorized",
                                        "You must log in with a valid username and password"))
                        // 403: logged in, but the role is not allowed
                        .accessDeniedHandler((request, response, e) ->
                                JsonErrorWriter.write(request, response, 403, "Forbidden",
                                        "Your role is not allowed to do this operation")));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
