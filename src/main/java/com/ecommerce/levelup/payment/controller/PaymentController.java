package com.ecommerce.levelup.payment.controller;

import com.ecommerce.levelup.payment.dto.PaymentDTO;
import com.ecommerce.levelup.payment.dto.ProcessPaymentRequest;
import com.ecommerce.levelup.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Pagos", description = "Sistema de pagos - Iniciar, confirmar y gestionar transacciones")
@RestController
@Slf4j
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Iniciar pago", description = "Genera un token de pago temporal para confirmar la transacci\u00f3n (requiere autenticaci\u00f3n)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token de pago generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inv\u00e1lidos o stock insuficiente")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        try {
            log.info("Received initiatePayment request: {}", paymentDTO);
            Map<String, Object> response = paymentService.initiatePayment(paymentDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error in initiatePayment for payload {}", paymentDTO, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Confirmar pago", description = "Confirma un pago pendiente usando el token generado (requiere payment token en header Authorization)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago confirmado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Token inv\u00e1lido, pago expirado o stock insuficiente"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestHeader("Authorization") String paymentToken) {
        try {
            log.info("Received confirmPayment request for id={} headerPresent={}", id, paymentToken != null && !paymentToken.isBlank());
            Map<String, Object> response = paymentService.confirmPayment(id, request, paymentToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Mis pagos", description = "Obtiene el historial de pagos del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de pagos del usuario")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/mis-pagos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentDTO>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getUserPayments());
    }

    @Operation(summary = "Obtener pago por ID", description = "Retorna los detalles de un pago espec\u00edfico (solo el propietario o admin)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
    @Operation(summary = "Todos los pagos (Admin)", description = "Obtiene el listado completo de todos los pagos del sistema (requiere ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista de todos los pagos")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @Operation(summary = "Reembolsar pago", description = "Devuelve el stock al inventario y marca el pago como reembolsado (requiere ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago reembolsado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Pago no puede ser reembolsado"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(summary = "Confirmar pago (Admin)", description = "Fuerza la confirmaci\u00f3n de un pago pendiente sin validaci\u00f3n de token (requiere ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago confirmado por administrador"),
        @ApiResponse(responseCode = "400", description = "Pago no puede ser confirmado"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/confirmar-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminConfirmPayment(@PathVariable Long id) {
        try {
            Map<String, Object> response = paymentService.adminConfirmPayment(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}