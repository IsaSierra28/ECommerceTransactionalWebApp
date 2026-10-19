package com.example.ecommerce.repository;

import com.example.ecommerce.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {

    List<CustomerOrder> findByCustomerIdOrderByOrderDateDesc(Integer customerId);

    List<CustomerOrder> findAllByOrderByOrderDateDesc();

    boolean existsByCustomerId(Integer customerId);

    // Orders that contain at least one product of the vendor
    @Query("SELECT DISTINCT i.order FROM OrderItem i WHERE i.product.vendor.id = :vendorId ORDER BY i.order.orderDate DESC")
    List<CustomerOrder> findOrdersByVendorId(Integer vendorId);
}
