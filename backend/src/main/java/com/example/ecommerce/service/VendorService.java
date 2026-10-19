package com.example.ecommerce.service;

import com.example.ecommerce.dto.VendorRequest;
import com.example.ecommerce.dto.VendorResponse;
import com.example.ecommerce.dto.VendorUpdateRequest;
import com.example.ecommerce.exception.ConflictException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Role;
import com.example.ecommerce.model.Vendor;
import com.example.ecommerce.repository.AppUserRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.VendorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public VendorService(VendorRepository vendorRepository,
                         AppUserRepository appUserRepository,
                         RoleRepository roleRepository,
                         ProductRepository productRepository,
                         PasswordEncoder passwordEncoder) {
        this.vendorRepository = vendorRepository;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> findAll() {
        return vendorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse findById(Integer id) {
        return toResponse(getVendor(id));
    }

    /**
     * Creates the user account (role VENDOR) and the vendor profile in the same transaction.
     */
    @Transactional
    public VendorResponse createVendor(VendorRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ConflictException("The username '" + request.username() + "' is already used");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("The email '" + request.email() + "' is already used");
        }
        if (vendorRepository.existsByCompanyName(request.companyName())) {
            throw new ConflictException("The company '" + request.companyName() + "' already exists");
        }

        Role vendorRole = roleRepository.findByName(UserService.VENDOR)
                .orElseThrow(() -> new ResourceNotFoundException("Role VENDOR not found"));

        AppUser user = new AppUser();
        user.setRole(vendorRole);
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        // The password is never saved as plain text
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        appUserRepository.save(user);

        Vendor vendor = new Vendor();
        vendor.setUser(user);
        vendor.setCompanyName(request.companyName().trim());
        vendor.setPhone(request.phone());
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public VendorResponse updateVendor(Integer id, VendorUpdateRequest request) {
        Vendor vendor = getVendor(id);
        AppUser user = vendor.getUser();

        if (!user.getEmail().equalsIgnoreCase(request.email()) && appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("The email '" + request.email() + "' is already used");
        }
        if (!vendor.getCompanyName().equalsIgnoreCase(request.companyName())
                && vendorRepository.existsByCompanyName(request.companyName())) {
            throw new ConflictException("The company '" + request.companyName() + "' already exists");
        }

        user.setEmail(request.email().trim());
        vendor.setCompanyName(request.companyName().trim());
        vendor.setPhone(request.phone());
        return toResponse(vendorRepository.save(vendor));
    }

    /**
     * The admin can activate or deactivate a vendor.
     * The products of an inactive vendor cannot be bought.
     */
    @Transactional
    public VendorResponse setActive(Integer id, boolean active) {
        Vendor vendor = getVendor(id);
        vendor.setActive(active);
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public void deleteVendor(Integer id) {
        Vendor vendor = getVendor(id);
        if (productRepository.existsByVendorId(id)) {
            throw new ConflictException("The vendor has products and cannot be deleted. Deactivate the vendor instead.");
        }
        AppUser user = vendor.getUser();
        vendorRepository.delete(vendor);
        appUserRepository.delete(user);
    }

    private Vendor getVendor(Integer id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor " + id + " not found"));
    }

    private VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getUser().getUsername(),
                vendor.getUser().getEmail(),
                vendor.getCompanyName(),
                vendor.getPhone(),
                vendor.getActive());
    }
}
