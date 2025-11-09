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

    // Información del Usuario
    @NotNull(message = "ID del Usuario")
    @Column(nullable = false)
    private Long userId;

    @NotBlank(message = "Email del Usuario")
    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(length = 100)
    private String userName;

    // Información de Productos (JSON String o texto)
    @Column(columnDefinition = "TEXT")
    private String productsJson; // Guardar JSON con productos [{id, name, quantity, price}, ...]

    // Montos
    @NotNull(message = "Monto total")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost;

    // Información de Pago
    @NotBlank(message = "Pago")
    @Column(nullable = false, length = 50)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.

    @Column(length = 100)
    private String cardLastFourDigits;

    @Column(length = 50)
    private String cardType; // VISA, MASTERCARD, etc.

    @Column(unique = true, length = 100)
    private String transactionId;

    @Column(unique = true, length = 100)
    private String paymentToken; // Token JWT para este pago

    // Estado del Pago
    @NotBlank(message = "Estado del Pago")
    @Column(nullable = false, length = 50)
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED

    @Column(length = 500)
    private String statusMessage;

    // Información de Envío
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

    // Fechas
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime refundedAt;

    // Información adicional
    @Column(length = 1000)
    private String notes;

    @Column(length = 50)
    private String ipAddress;

    // Estados de pago predefinidos
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Métodos de pago predefinidos
    public static final String METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String METHOD_DEBIT_CARD = "DEBIT_CARD";
    public static final String METHOD_PAYPAL = "PAYPAL";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
}