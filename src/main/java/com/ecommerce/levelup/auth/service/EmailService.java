package com.ecommerce.levelup.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("[stub] Password reset link for {} -> {}", to, resetLink);
    }
}
