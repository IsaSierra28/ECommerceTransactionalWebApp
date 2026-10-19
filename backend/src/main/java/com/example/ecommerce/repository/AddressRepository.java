package com.example.ecommerce.repository;

import com.example.ecommerce.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByCustomerId(Integer customerId);

    void deleteByCustomerId(Integer customerId);
}
