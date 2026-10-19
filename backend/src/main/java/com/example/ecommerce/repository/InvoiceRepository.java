package com.example.ecommerce.repository;

import com.example.ecommerce.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    Optional<Invoice> findByOrderId(Integer orderId);

    List<Invoice> findByOrderCustomerIdOrderByIssueDateDesc(Integer customerId);
}
