package com.ecommerce.levelup.payment.service;

import com.ecommerce.levelup.payment.dto.PaymentDTO;
import com.ecommerce.levelup.payment.dto.ProcessPaymentRequest;
import com.ecommerce.levelup.payment.model.Payment;
import com.ecommerce.levelup.payment.repository.PaymentRepository;
import com.ecommerce.levelup.security.JwtUtil;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

   
    @Transactional
    public Map<String, Object> initiatePayment(PaymentDTO paymentDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (paymentDTO.getTotalAmount() == null || paymentDTO.getTotalAmount().doubleValue() <= 0) {
            throw new RuntimeException("El monto total debe ser mayor a 0");
        }

        if (paymentDTO.getProducts() == null || paymentDTO.getProducts().isEmpty()) {
            throw new RuntimeException("Debe incluir al menos un producto");
        }

        Payment payment = new Payment();
        payment.setUserId(user.getId());
        payment.setUserEmail(paymentDTO.getUserEmail() != null ? paymentDTO.getUserEmail() : user.getEmail());
        payment.setUserName(paymentDTO.getUserName() != null ? paymentDTO.getUserName() :
                user.getFirstName() + " " + user.getLastName());
        payment.setTotalAmount(paymentDTO.getTotalAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setStatus("PENDIENTE");
        payment.setShippingAddress(paymentDTO.getShippingAddress());
        payment.setShippingCity(paymentDTO.getShippingCity());
        payment.setShippingCountry(paymentDTO.getShippingCountry());
        payment.setShippingPostalCode(paymentDTO.getShippingPostalCode());
        payment.setShippingPhone(paymentDTO.getShippingPhone());
        payment.setShippingCost(paymentDTO.getShippingCost());
        payment.setTaxAmount(paymentDTO.getTaxAmount());
        payment.setCreatedAt(LocalDateTime.now());

        try {
            String productsJson = objectMapper.writeValueAsString(paymentDTO.getProducts());
            payment.setProductsJson(productsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al procesar los productos");
        }

        String paymentToken = jwtUtil.generatePaymentToken(payment.getId(), user.getUsername());
        payment.setPaymentToken(paymentToken);

        Payment saved = paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("pagoId", saved.getId());
        response.put("tokenPago", paymentToken);
        response.put("estado", saved.getStatus());
        response.put("montoTotal", saved.getTotalAmount());
        response.put("mensaje", "Pago iniciado exitosamente. Use el token para confirmar el pago.");

        return response;
    }

    
    @Transactional
    public Map<String, Object> confirmPayment(Long paymentId, ProcessPaymentRequest request, String paymentToken) {
        String cleanToken = paymentToken.replace("Bearer ", "");
        if (!jwtUtil.validatePaymentToken(cleanToken)) {
            throw new RuntimeException("Token de pago inválido o expirado");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + paymentId));

        if (!"PENDIENTE".equals(payment.getStatus())) {
            throw new RuntimeException("El pago ya fue procesado o cancelado");
        }

        if (!cleanToken.equals(payment.getPaymentToken())) {
            throw new RuntimeException("El token no corresponde a este pago");
        }

        boolean paymentSuccess = processPaymentSimulation(request);

        if (paymentSuccess) {
            payment.setStatus("COMPLETADO");
            payment.setStatusMessage("Pago procesado exitosamente");
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString());
            payment.setCompletedAt(LocalDateTime.now());
            payment.setCardType(request.getCardType());
            payment.setCardLastFourDigits(request.getCardNumber().substring(request.getCardNumber().length() - 4));
        } else {
            payment.setStatus("FALLIDO");
            payment.setStatusMessage("El pago fue rechazado. Verifique los datos de su tarjeta.");
        }

        Payment updated = paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("exitoso", paymentSuccess);
        response.put("mensaje", updated.getStatusMessage());
        response.put("idTransaccion", updated.getTransactionId());
        response.put("estado", updated.getStatus());

        return response;
    }

   
    public List<PaymentDTO> getUserPayments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

   
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!payment.getUserId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("No tiene permiso para ver este pago");
        }

        return convertToDTO(payment);
    }

   
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));

        if (!"COMPLETADO".equals(payment.getStatus())) {
            throw new RuntimeException("Solo se pueden reembolsar pagos completados");
        }

        if (payment.getRefundedAt() != null) {
            throw new RuntimeException("Este pago ya fue reembolsado");
        }

        payment.setStatus("REEMBOLSADO");
        payment.setRefundedAt(LocalDateTime.now());
        payment.setStatusMessage("Pago reembolsado exitosamente");
        paymentRepository.save(payment);
    }

    private boolean processPaymentSimulation(ProcessPaymentRequest request) {
    
        return request.getCardNumber() != null &&
                request.getCardNumber().replaceAll("\\s", "").length() == 16;
    }

   
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUserId());
        dto.setUserEmail(payment.getUserEmail());
        dto.setUserName(payment.getUserName());
        dto.setTotalAmount(payment.getTotalAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setStatusMessage(payment.getStatusMessage());
        dto.setTransactionId(payment.getTransactionId());
        dto.setShippingAddress(payment.getShippingAddress());
        dto.setShippingCity(payment.getShippingCity());
        dto.setShippingCountry(payment.getShippingCountry());
        dto.setShippingPostalCode(payment.getShippingPostalCode());
        dto.setShippingPhone(payment.getShippingPhone());
        dto.setShippingCost(payment.getShippingCost());
        dto.setTaxAmount(payment.getTaxAmount());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setCompletedAt(payment.getCompletedAt());
        dto.setRefundedAt(payment.getRefundedAt());

        if (payment.getProductsJson() != null) {
            try {
                List<?> products = objectMapper.readValue(payment.getProductsJson(), List.class);
                dto.setProducts(products);
            } catch (JsonProcessingException e) {
               
            }
        }

        return dto;
    }
}