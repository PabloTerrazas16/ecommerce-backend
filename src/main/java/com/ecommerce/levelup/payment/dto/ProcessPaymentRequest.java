package com.ecommerce.levelup.payment.dto;

import lombok.Data;

@Data
public class ProcessPaymentRequest {
    private String cardNumber;
    private String cardholderName;
    private String expirationMonth;
    private String expirationYear;
    private String cvv;
    private String cardType; // VISA, MASTERCARD, AMEX, etc.
}