package com.ecommerce.levelup.auth.service;

import com.ecommerce.levelup.auth.model.PasswordResetToken;
import com.ecommerce.levelup.auth.repository.PasswordResetTokenRepository;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    private static final SecureRandom secureRandom = new SecureRandom();

    private String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format(Locale.ROOT, "%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to hash token", e);
        }
    }

    private String generateToken() {
        byte[] b = new byte[32];
        secureRandom.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Transactional
    public Map<String, Object> createPasswordResetForEmail(String email) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("mensaje", "Si el correo existe, se enviaron instrucciones para restablecer la contraseña.");

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return msg;
        }

        User user = userOpt.get();

        String token = generateToken();
        String tokenHash = hash(token);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusMinutes(Long.parseLong(env.getProperty("app.reset.token.minutes", "30")));

        PasswordResetToken prt = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .createdAt(now)
                .expiresAt(expires)
                .used(false)
                .build();

        tokenRepository.save(prt);

        String frontendBase = env.getProperty("app.frontend.url", "http://localhost:5173");
        String resetPath = env.getProperty("app.frontend.resetPath", "/react-ecommerce/reset-password");
        String resetLink = frontendBase + resetPath + "?token=" + token;

        String webhook = env.getProperty("app.webhook.url");
        if (webhook != null && !webhook.isBlank()) {
            try {
                String title = "Restablecer contraseña";
                String description = String.format("Se ha solicitado un restablecimiento de contraseña para **%s**.\nSi no solicitaste esto, ignora este mensaje.", user.getEmail());

                Map<String, Object> field = Map.of(
                    "name", "Enlace de restablecimiento",
                    "value", resetLink,
                    "inline", false
                );

                Map<String, Object> embed = Map.of(
                    "title", title,
                    "description", description,
                    "color", 16753920,
                    "fields", List.of(field),
                    "timestamp", Instant.now().toString()
                );

                Map<String, Object> payload = Map.of(
                    "content", "", // short plain content optional
                    "embeds", List.of(embed)
                );

                String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);

                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(webhook))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

                log.info("Sending password reset webhook to {}", webhook);

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(r -> log.info("Posted reset link to webhook {}, status {}, body: {}", webhook, r.statusCode(), r.body()))
                    .exceptionally(ex -> { log.error("Failed posting reset link to webhook {}", webhook, ex); return null; });
            } catch (Exception e) {
                log.error("Error preparing webhook payload", e);
            }
        }

        // If running with dev profile, optionally return token for testing
        boolean isDev = false;
        for (String p : env.getActiveProfiles()) if (p.equalsIgnoreCase("dev")) isDev = true;
        if (isDev) {
            Map<String, Object> devResp = new HashMap<>();
            devResp.put("mensaje", "Token generado (dev)");
            devResp.put("token", token);
            return devResp;
        }

        return msg;
    }

    @Transactional
    public Map<String, Object> resetPasswordWithToken(String token, String newPassword) {
        String tokenHash = hash(token);

        // Fetch with pessimistic write lock to avoid race conditions (single-use guarantee)
        PasswordResetToken prt = tokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        if (Boolean.TRUE.equals(prt.getUsed())) {
            throw new RuntimeException("Token ya fue utilizado");
        }

        if (prt.getExpiresAt() != null && prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        User user = userRepository.findById(prt.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el token"));

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove the token after successful use so it cannot be reused
        tokenRepository.delete(prt);

        // Optionally revoke sessions / tokens here (not implemented)

        return Map.of("mensaje", "Contraseña actualizada exitosamente");
    }
}
