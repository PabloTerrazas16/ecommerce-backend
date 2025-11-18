package com.ecommerce.levelup.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecoverRequest {
    @NotBlank
    @Email
    private String email;
}
