package com.ecommerce.levelup.payment.controller;

import com.ecommerce.levelup.payment.dto.*;
import com.ecommerce.levelup.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Generar token de pago (requiere autenticación)
     * POST /api/payments/token
     */
    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> generatePaymentToken(@Valid @RequestBody PaymentTokenRequest request) {
        try {
            PaymentTokenResponse response = paymentService.generatePaymentToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Procesar pago (requiere token de pago válido)
     * POST /api/payments/process
     */
    @PostMapping("/process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        try {
            PaymentDTO payment = paymentService.processPayment(request);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Obtener pago por ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @paymentService.isPaymentOwner(#id, authentication.principal.id)")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Obtener pagos de un usuario
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<PaymentDTO>> getUserPayments(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    /**
     * Obtener historial de pagos de un usuario
     * GET /api/payments/user/{userId}/history
     */
    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<PaymentDTO>> getUserPaymentHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getUserPaymentHistory(userId));
    }

    /**
     * Obtener todos los pagos (solo ADMIN)
     * GET /api/payments
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Obtener pagos por estado (solo ADMIN)
     * GET /api/payments/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    /**
     * Reembolsar pago (solo ADMIN)
     * PUT /api/payments/{id}/refund
     */
    @PutMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundPayment(@PathVariable Long id) {
        try {
            PaymentDTO refunded = paymentService.refundPayment(id);
            return ResponseEntity.ok(refunded);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Cancelar pago
     * PUT /api/payments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @paymentService.isPaymentOwner(#id, authentication.principal.id)")
    public ResponseEntity<?> cancelPayment(@PathVariable Long id) {
        try {
            PaymentDTO cancelled = paymentService.cancelPayment(id);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}