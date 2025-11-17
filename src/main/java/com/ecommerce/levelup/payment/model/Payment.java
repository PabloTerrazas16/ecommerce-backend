package com.ecommerce.levelup.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "ID del Usuario")
    @Column(nullable = false)
    private Long userId;

    @NotBlank(message = "Email del Usuario")
    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(length = 100)
    private String userName;

    @Column(columnDefinition = "TEXT")
    private String productsJson; 


    @NotNull(message = "Monto total")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @NotBlank(message = "Pago")
    @Column(nullable = false, length = 50)
    private String paymentMethod; 

    @Column(length = 100)
    private String cardLastFourDigits;

    @Column(length = 50)
    private String cardType;

    @Column(unique = true, length = 100)
    private String transactionId;

    @Column(unique = true, length = 1000)
    private String paymentToken;

    @NotBlank(message = "Estado del Pago")
    @Column(nullable = false, length = 50)
    private String status; 

    @Column(length = 500)
    private String statusMessage;

    @Column(length = 200)
    private String shippingAddress;

    @Column(length = 100)
    private String shippingCity;

    @Column(length = 100)
    private String shippingCountry;

    @Column(length = 20)
    private String shippingPostalCode;

    @Column(length = 20)
    private String shippingPhone;

  
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime refundedAt;

    @Column(length = 1000)
    private String notes;

    @Column(length = 50)
    private String ipAddress;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String METHOD_DEBIT_CARD = "DEBIT_CARD";
    public static final String METHOD_PAYPAL = "PAYPAL";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
}