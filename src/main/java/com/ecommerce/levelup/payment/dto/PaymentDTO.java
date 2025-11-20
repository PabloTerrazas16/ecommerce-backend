package com.ecommerce.levelup.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private String statusMessage;
    private String transactionId;
    private String shippingAddress;
    private String shippingCity;
    private String shippingCountry;
    private String shippingPostalCode;
    private String shippingPhone;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime refundedAt;
    
    // Campo para productos
    private List<?> products;
}