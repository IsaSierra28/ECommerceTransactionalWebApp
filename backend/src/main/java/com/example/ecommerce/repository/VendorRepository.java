package com.example.ecommerce.repository;

import com.example.ecommerce.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    Optional<Vendor> findByUserUsername(String username);

    boolean existsByCompanyName(String companyName);
}
