package com.ecommerce.levelup.payment.repository;

import com.ecommerce.levelup.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Buscar pagos de un usuario ordenados por fecha descendente
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Buscar todos los pagos ordenados por fecha descendente
    List<Payment> findAllByOrderByCreatedAtDesc();
    
    // Buscar pagos por estado
    List<Payment> findByStatus(String status);
    
    // Buscar pagos por email de usuario
    List<Payment> findByUserEmail(String userEmail);
}