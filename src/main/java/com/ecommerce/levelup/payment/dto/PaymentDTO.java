package com.ecommerce.levelup.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String userEmail;
    private String userName;

    private String productsJson;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;

    private BigDecimal taxAmount;
    private BigDecimal shippingCost;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String cardLastFourDigits;
    private String cardType;
    private String transactionId;
    private String paymentToken;

    @NotBlank(message = "Status is required")
    private String status;

    private String statusMessage;

    private String shippingAddress;
    private String shippingCity;
    private String shippingCountry;
    private String shippingPostalCode;
    private String shippingPhone;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime refundedAt;

    private String notes;
}