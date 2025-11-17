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

    public enum TokenType {
        USER, PAYMENT
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.USER.name());
        return createToken(claims, username, expiration);
    }

    public String generatePaymentToken(Long paymentId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("paymentId", paymentId);
        claims.put("type", TokenType.PAYMENT.name());
        return createToken(claims, username, paymentExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public TokenType getTokenType(String token) {
        Claims claims = extractAllClaims(token);
        String type = claims.get("type", String.class);
        return TokenType.valueOf(type);
    }

    public String extractUsername(String token, TokenType expectedType) {
        TokenType actualType = getTokenType(token);
        if (actualType != expectedType) {
            throw new IllegalArgumentException("Token type mismatch");
        }
        return getUsernameFromToken(token);
    }

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

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validatePaymentToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return TokenType.PAYMENT.name().equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public Long getPaymentIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("paymentId", Long.class);
    }
}