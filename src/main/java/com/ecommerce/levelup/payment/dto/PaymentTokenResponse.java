package com.ecommerce.levelup.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTokenResponse {

    private String paymentToken;
    private Long expiresIn; // en milisegundos
    private String message;
}