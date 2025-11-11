package com.ecommerce.levelup.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotBlank(message = "Payment token is required")
    private String paymentToken;

    private String cardNumber;
    private String cardHolderName;
    private String expirationDate;
    private String cvv;

    private String shippingAddress;
    private String shippingCity;
    private String shippingCountry;
    private String shippingPostalCode;
    private String shippingPhone;

    private String notes;
}