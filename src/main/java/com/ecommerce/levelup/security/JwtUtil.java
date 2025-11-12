package com.ecommerce.levelup.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.payment.expiration:300000}")
    private Long paymentExpiration;

    // Enum para tipos de token
    public enum TokenType {
        USER, PAYMENT
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generar token para autenticación
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.USER.name());
        return createToken(claims, username, expiration);
    }

    // Generar token para pagos
    public String generatePaymentToken(Long paymentId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("paymentId", paymentId);
        claims.put("type", TokenType.PAYMENT.name());
        return createToken(claims, username, paymentExpiration);
    }

    // Crear token
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    // Obtener tipo de token
    public TokenType getTokenType(String token) {
        Claims claims = extractAllClaims(token);
        String type = claims.get("type", String.class);
        return TokenType.valueOf(type);
    }

    // Extraer username del token (sin importar el tipo)
    public String extractUsername(String token, TokenType expectedType) {
        TokenType actualType = getTokenType(token);
        if (actualType != expectedType) {
            throw new IllegalArgumentException("Token type mismatch");
        }
        return getUsernameFromToken(token);
    }

    // Validar token de usuario
    public Boolean validateUserToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            TokenType type = getTokenType(token);
            return (username.equals(userDetails.getUsername()) && 
                    !isTokenExpired(token) && 
                    type == TokenType.USER);
        } catch (Exception e) {
            return false;
        }
    }

    // Validar token
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Validar token de pago
    public Boolean validatePaymentToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return TokenType.PAYMENT.name().equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Obtener username del token
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Obtener fecha de expiración
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer un claim específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraer todos los claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Verificar si el token está expirado
    private Boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    // Obtener ID de pago del token
    public Long getPaymentIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("paymentId", Long.class);
    }
}