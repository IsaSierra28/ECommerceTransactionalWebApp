package com.example.ecommerce.repository;

import com.example.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByOrderId(Integer orderId);
}
