package com.ecommerce.levelup.payment.service;

import com.ecommerce.levelup.payment.dto.*;
import com.ecommerce.levelup.payment.model.Payment;
import com.ecommerce.levelup.payment.repository.PaymentRepository;
import com.ecommerce.levelup.security.JwtUtil;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Generar token de pago
     */
    public PaymentTokenResponse generatePaymentToken(PaymentTokenRequest request) {
        // Validar usuario
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Generar ID único de pago
        String paymentId = UUID.randomUUID().toString();

        // Crear registro de pago pendiente
        Payment payment = new Payment();
        payment.setUserId(user.getId());
        payment.setUserEmail(user.getEmail());
        payment.setUserName(user.getFullName());
        payment.setProductsJson(request.getProductsJson());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(Payment.STATUS_PENDING);
        payment.setStatusMessage("Payment token generated, awaiting processing");
        payment.setTransactionId(paymentId);

        // Generar token JWT de pago
        String paymentToken = jwtUtil.generatePaymentToken(
                user.getId(),
                paymentId,
                request.getTotalAmount().doubleValue()
        );

        payment.setPaymentToken(paymentToken);

        // Guardar pago
        paymentRepository.save(payment);

        // Preparar respuesta
        PaymentTokenResponse response = new PaymentTokenResponse();
        response.setPaymentToken(paymentToken);
        response.setExpiresIn(jwtUtil.getExpirationTime(JwtUtil.TokenType.PAYMENT));
        response.setMessage("Payment token generated successfully. Token is valid for " +
                (jwtUtil.getExpirationTime(JwtUtil.TokenType.PAYMENT) / 60000) + " minutes.");

        return response;
    }

    /**
     * Procesar pago
     */
    public PaymentDTO processPayment(ProcessPaymentRequest request) {
        // Validar token de pago
        if (!jwtUtil.validatePaymentToken(request.getPaymentToken())) {
            throw new RuntimeException("Invalid or expired payment token");
        }

        // Buscar pago por token
        Payment payment = paymentRepository.findByPaymentToken(request.getPaymentToken())
                .orElseThrow(() -> new RuntimeException("Payment not found for token"));

        // Verificar que el pago esté en estado PENDING
        if (!payment.getStatus().equals(Payment.STATUS_PENDING)) {
            throw new RuntimeException("Payment already processed or cancelled");
        }

        try {
            // Aquí iría la lógica real de procesamiento de pago
            // Por ahora, simulamos un pago exitoso

            // Actualizar información de pago
            if (request.getCardNumber() != null) {
                payment.setCardLastFourDigits(request.getCardNumber().substring(
                        Math.max(0, request.getCardNumber().length() - 4)));
            }

            payment.setShippingAddress(request.getShippingAddress());
            payment.setShippingCity(request.getShippingCity());
            payment.setShippingCountry(request.getShippingCountry());
            payment.setShippingPostalCode(request.getShippingPostalCode());
            payment.setShippingPhone(request.getShippingPhone());
            payment.setNotes(request.getNotes());

            // Marcar como completado
            payment.setStatus(Payment.STATUS_COMPLETED);
            payment.setStatusMessage("Payment processed successfully");
            payment.setCompletedAt(LocalDateTime.now());

            // Guardar
            Payment processedPayment = paymentRepository.save(payment);

            return convertToDTO(processedPayment);

        } catch (Exception e) {
            // Si hay error, marcar como fallido
            payment.setStatus(Payment.STATUS_FAILED);
            payment.setStatusMessage("Payment processing failed: " + e.getMessage());
            paymentRepository.save(payment);

            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Obtener pago por ID
     */
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return convertToDTO(payment);
    }

    /**
     * Obtener pagos de un usuario
     */
    public List<PaymentDTO> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener historial de pagos de un usuario
     */
    public List<PaymentDTO> getUserPaymentHistory(Long userId) {
        return paymentRepository.findUserPaymentHistory(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pagos por estado
     */
    public List<PaymentDTO> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los pagos
     */
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Reembolsar pago
     */
    public PaymentDTO refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getStatus().equals(Payment.STATUS_COMPLETED)) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        // Procesar reembolso (aquí iría la lógica real)
        payment.setStatus(Payment.STATUS_REFUNDED);
        payment.setStatusMessage("Payment refunded");
        payment.setRefundedAt(LocalDateTime.now());

        Payment refundedPayment = paymentRepository.save(payment);
        return convertToDTO(refundedPayment);
    }

    /**
     * Cancelar pago
     */
    public PaymentDTO cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getStatus().equals(Payment.STATUS_PENDING)) {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        payment.setStatus(Payment.STATUS_CANCELLED);
        payment.setStatusMessage("Payment cancelled by user");

        Payment cancelledPayment = paymentRepository.save(payment);
        return convertToDTO(cancelledPayment);
    }

    /**
     * Convertir Payment a PaymentDTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUserId());
        dto.setUserEmail(payment.getUserEmail());
        dto.setUserName(payment.getUserName());
        dto.setProductsJson(payment.getProductsJson());
        dto.setTotalAmount(payment.getTotalAmount());
        dto.setTaxAmount(payment.getTaxAmount());
        dto.setShippingCost(payment.getShippingCost());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setCardLastFourDigits(payment.getCardLastFourDigits());
        dto.setCardType(payment.getCardType());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentToken(payment.getPaymentToken());
        dto.setStatus(payment.getStatus());
        dto.setStatusMessage(payment.getStatusMessage());
        dto.setShippingAddress(payment.getShippingAddress());
        dto.setShippingCity(payment.getShippingCity());
        dto.setShippingCountry(payment.getShippingCountry());
        dto.setShippingPostalCode(payment.getShippingPostalCode());
        dto.setShippingPhone(payment.getShippingPhone());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setCompletedAt(payment.getCompletedAt());
        dto.setRefundedAt(payment.getRefundedAt());
        dto.setNotes(payment.getNotes());
        return dto;
    }
}