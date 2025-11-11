package com.ecommerce.levelup.payment.repository;

import com.ecommerce.levelup.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByUserEmail(String userEmail);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByPaymentToken(String paymentToken);

    List<Payment> findByStatus(String status);

    List<Payment> findByPaymentMethod(String paymentMethod);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findUserPaymentHistory(Long userId);
}