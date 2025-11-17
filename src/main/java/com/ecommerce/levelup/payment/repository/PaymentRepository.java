package com.ecommerce.levelup.payment.repository;

import com.ecommerce.levelup.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Payment> findAllByOrderByCreatedAtDesc();
    
    List<Payment> findByStatus(String status);
    
    List<Payment> findByUserEmail(String userEmail);
}