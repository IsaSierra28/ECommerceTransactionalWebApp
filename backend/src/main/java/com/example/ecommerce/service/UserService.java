package com.example.ecommerce.service;

import com.example.ecommerce.dto.UserInfoResponse;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Small service used by the other services to know who is the logged user and which role they have.
 */
@Service
public class UserService {

    public static final String ADMIN = "ADMIN";
    public static final String VENDOR = "VENDOR";
    public static final String CUSTOMER = "CUSTOMER";

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User '" + username + "' not found"));
    }

    @Transactional(readOnly = true)
    public String getRole(String username) {
        return getByUsername(username).getRole().getName();
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String username) {
        AppUser user = getByUsername(username);
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().getName());
    }
}
