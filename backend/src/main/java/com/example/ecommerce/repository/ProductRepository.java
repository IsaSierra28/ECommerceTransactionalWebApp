package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByVendorId(Integer vendorId);

    boolean existsBySku(String sku);

    boolean existsByVendorId(Integer vendorId);
}
