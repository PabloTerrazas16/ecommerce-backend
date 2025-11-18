package com.ecommerce.levelup.payment.service;

import com.ecommerce.levelup.payment.dto.PaymentDTO;
import com.ecommerce.levelup.payment.dto.ProcessPaymentRequest;
import com.ecommerce.levelup.payment.model.Payment;
import com.ecommerce.levelup.product.model.Product;
import com.ecommerce.levelup.product.repository.ProductRepository;
import com.ecommerce.levelup.payment.repository.PaymentRepository;
import com.ecommerce.levelup.security.JwtUtil;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

   
    @Transactional
    public Map<String, Object> initiatePayment(PaymentDTO paymentDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        log.info("Initiating payment for user {} with payload: {}", username, paymentDTO);

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
        payment.setStatus(Payment.STATUS_PENDING);
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

        Payment saved = paymentRepository.save(payment);

        String paymentToken = jwtUtil.generatePaymentToken(saved.getId(), user.getUsername());
        saved.setPaymentToken(paymentToken);

        saved = paymentRepository.save(saved);

        log.info("Payment saved with id={}, token={}", saved.getId(), paymentToken);

        Map<String, Object> response = new HashMap<>();
        response.put("pagoId", saved.getId());
        response.put("tokenPago", paymentToken);
        response.put("estado", saved.getStatus());
        response.put("montoTotal", saved.getTotalAmount());
        response.put("mensaje", "Pago iniciado exitosamente. Use el token para confirmar el pago.");

        response.put("id", saved.getId());
        response.put("paymentId", saved.getId());
        response.put("paymentToken", paymentToken);
        response.put("status", saved.getStatus());
        response.put("totalAmount", saved.getTotalAmount());

        return response;
    }

    
    @Transactional
    public Map<String, Object> confirmPayment(Long paymentId, ProcessPaymentRequest request, String paymentToken) {
        String cleanToken = paymentToken != null ? paymentToken.replace("Bearer ", "") : null;
        boolean validPaymentToken = cleanToken != null && jwtUtil.validatePaymentToken(cleanToken);
        log.info("Validating payment token for paymentId={} tokenPresent={} valid={}", paymentId, cleanToken != null && !cleanToken.isBlank(), validPaymentToken);
        if (!validPaymentToken) {
            throw new RuntimeException("Token de pago inválido o expirado");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + paymentId));

        if (!Payment.STATUS_PENDING.equals(payment.getStatus())) {
            throw new RuntimeException("El pago ya fue procesado o cancelado");
        }

        if (!cleanToken.equals(payment.getPaymentToken())) {
            throw new RuntimeException("El token no corresponde a este pago");
        }

        boolean paymentSuccess = processPaymentSimulation(request);
        log.info("Payment simulation result for paymentId={} cardPresent={} success={}", paymentId, request.getCardNumber() != null, paymentSuccess);

        if (paymentSuccess) {
            payment.setStatus(Payment.STATUS_COMPLETED);
            payment.setStatusMessage("Pago procesado exitosamente");
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString());
            payment.setCompletedAt(LocalDateTime.now());
            payment.setCardType(request.getCardType());
            payment.setCardLastFourDigits(request.getCardNumber().substring(request.getCardNumber().length() - 4));

            // Reducir stock de los productos incluidos en el pago
            if (payment.getProductsJson() != null) {
                try {
                    List<?> products = objectMapper.readValue(payment.getProductsJson(), List.class);
                    log.info("Reducing stock for payment id={} products={}", payment.getId(), products);
                    for (Object raw : products) {
                        if (!(raw instanceof java.util.Map)) continue;
                        java.util.Map map = (java.util.Map) raw;
                        // try to obtain an id and quantity
                        Object idObj = map.get("id");
                        Object qtyObj = map.get("quantity");
                        Long prodId = null;
                        Integer qty = null;
                        if (idObj != null) {
                            try {
                                prodId = Long.valueOf(String.valueOf(idObj));
                            } catch (NumberFormatException nfe) {
                                prodId = null;
                            }
                        }
                        if (qtyObj != null) {
                            try {
                                qty = Integer.valueOf(String.valueOf(qtyObj));
                            } catch (NumberFormatException nfe) {
                                qty = null;
                            }
                        }

                        if (prodId != null && qty != null && qty > 0) {
                            try {
                                Product prod = productRepository.findById(prodId).orElse(null);
                                if (prod == null) {
                                    log.warn("Product id={} not found while processing payment id={}", prodId, payment.getId());
                                } else {
                                    log.info("Attempting atomic decrease stock for product id={} by {} (current={})", prodId, qty, prod.getStock());
                                    int updated = productRepository.decreaseStockIfAvailable(prodId, qty);
                                    if (updated <= 0) {
                                        log.error("Not enough stock for product id={} (requested={})", prodId, qty);
                                        throw new RuntimeException("No hay suficiente stock para el producto id=" + prodId);
                                    }
                                    int newStock = prod.getStock() - qty;
                                    log.info("Stock decreased for product id={} newStock={}", prodId, newStock);
                                }
                            } catch (Exception exInner) {
                                log.error("Failed decreasing stock for product id={}: {}", prodId, exInner.getMessage());
                                throw new RuntimeException("No hay suficiente stock para el producto id=" + prodId);
                            }
                        }
                    }
                    try {
                        productRepository.flush();
                        log.info("Flushed product updates to DB for payment id={}", payment.getId());
                    } catch (Exception flushEx) {
                        log.error("Failed flushing product updates: {}", flushEx.getMessage(), flushEx);
                        throw new RuntimeException("Error al persistir cambios de stock: " + flushEx.getMessage(), flushEx);
                    }
                } catch (Exception ex) {
                    // si falla la reducción de stock, registrar y continuar (la transacción hará rollback si se lanza)
                    throw new RuntimeException("Error al actualizar stock de productos: " + ex.getMessage(), ex);
                }
            }
        } else {
            payment.setStatus(Payment.STATUS_FAILED);
            payment.setStatusMessage("El pago fue rechazado. Verifique los datos de su tarjeta.");
        }

        Payment updated = paymentRepository.save(payment);
        try {
            paymentRepository.flush();
            log.info("Payment updated and flushed id={} status={}", updated.getId(), updated.getStatus());
        } catch (Exception e) {
            log.error("Failed flushing payment update id={}: {}", updated.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al persistir pago: " + e.getMessage(), e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("exitoso", paymentSuccess);
        response.put("mensaje", updated.getStatusMessage());
        response.put("idTransaccion", updated.getTransactionId());
        response.put("estado", updated.getStatus());

        return response;
    }

    @Transactional
    public Map<String, Object> adminConfirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + paymentId));

        if (!Payment.STATUS_PENDING.equals(payment.getStatus())) {
            throw new RuntimeException("Solo se pueden confirmar pagos en estado PENDING");
        }

        payment.setStatus(Payment.STATUS_COMPLETED);
        payment.setStatusMessage("Pago confirmado manualmente por administrador");
        payment.setTransactionId("ADMIN-TXN-" + UUID.randomUUID().toString());
        payment.setCompletedAt(LocalDateTime.now());

        if (payment.getProductsJson() != null) {
            try {
                List<?> products = objectMapper.readValue(payment.getProductsJson(), List.class);
                for (Object raw : products) {
                    if (!(raw instanceof java.util.Map)) continue;
                    java.util.Map map = (java.util.Map) raw;
                    Object idObj = map.get("id");
                    Object qtyObj = map.get("quantity");
                    Long prodId = null;
                    Integer qty = null;
                    if (idObj != null) {
                        try { prodId = Long.valueOf(String.valueOf(idObj)); } catch (NumberFormatException nfe) { prodId = null; }
                    }
                    if (qtyObj != null) {
                        try { qty = Integer.valueOf(String.valueOf(qtyObj)); } catch (NumberFormatException nfe) { qty = null; }
                    }

                    if (prodId != null && qty != null && qty > 0) {
                        int updated = productRepository.decreaseStockIfAvailable(prodId, qty);
                        if (updated <= 0) {
                            throw new RuntimeException("No hay suficiente stock para el producto id=" + prodId);
                        }
                    }
                }
                productRepository.flush();
            } catch (Exception ex) {
                log.error("Error al disminuir stock en confirmación admin para pago id={}: {}", payment.getId(), ex.getMessage(), ex);
                throw new RuntimeException("Error al actualizar stock de productos: " + ex.getMessage(), ex);
            }
        }

        Payment updated = paymentRepository.save(payment);
        paymentRepository.flush();

        Map<String, Object> response = new HashMap<>();
        response.put("exitoso", true);
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

        if (!Payment.STATUS_COMPLETED.equals(payment.getStatus())) {
            throw new RuntimeException("Solo se pueden reembolsar pagos completados");
        }

        if (payment.getRefundedAt() != null) {
            throw new RuntimeException("Este pago ya fue reembolsado");
        }

        payment.setStatus(Payment.STATUS_REFUNDED);
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