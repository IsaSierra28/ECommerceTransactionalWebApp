package com.example.ecommerce.controller;

import com.example.ecommerce.dto.UserInfoResponse;
import com.example.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Check the login and get the role of the logged user")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the logged user",
            description = "Returns the username and role if the username/password are correct. "
                    + "The React app will use it for the login page.")
    @ApiResponse(responseCode = "401", description = "Wrong username or password")
    public UserInfoResponse me(Principal principal) {
        return userService.getUserInfo(principal.getName());
    }
}
