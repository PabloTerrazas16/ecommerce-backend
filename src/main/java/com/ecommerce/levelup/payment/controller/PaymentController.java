package com.ecommerce.levelup.payment.controller;

import com.ecommerce.levelup.payment.dto.PaymentDTO;
import com.ecommerce.levelup.payment.dto.ProcessPaymentRequest;
import com.ecommerce.levelup.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Iniciar pago
     * POST /api/pagos
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        try {
            Map<String, Object> response = paymentService.initiatePayment(paymentDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Confirmar pago
     * POST /api/pagos/{id}/confirmar
     */
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestHeader("Authorization") String paymentToken) {
        try {
            Map<String, Object> response = paymentService.confirmPayment(id, request, paymentToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Obtener mis pagos
     * GET /api/pagos/mis-pagos
     */
    @GetMapping("/mis-pagos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentDTO>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getUserPayments());
    }

    /**
     * Obtener pago por ID
     * GET /api/pagos/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    /**
     * Obtener todos los pagos (Solo ADMIN)
     * GET /api/pagos
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Reembolsar pago (Solo ADMIN)
     * POST /api/pagos/{id}/reembolsar
     */
    @PostMapping("/{id}/reembolsar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundPayment(@PathVariable Long id) {
        try {
            paymentService.refundPayment(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pago reembolsado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}