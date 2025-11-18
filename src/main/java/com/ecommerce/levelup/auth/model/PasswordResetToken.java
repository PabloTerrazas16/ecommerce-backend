package com.ecommerce.levelup.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, length = 256)
    private String tokenHash;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "used")
    private Boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
