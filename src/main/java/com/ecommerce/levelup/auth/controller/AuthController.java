package com.ecommerce.levelup.auth.controller;

import com.ecommerce.levelup.auth.dto.LoginRequest;
import com.ecommerce.levelup.auth.dto.LoginResponse;
import com.ecommerce.levelup.auth.dto.RegisterRequest;
import com.ecommerce.levelup.auth.service.AuthService;
import com.ecommerce.levelup.user.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/autenticacion")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Registrar nuevo usuario
     * POST /api/autenticacion/registrar
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("username", request.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Login de usuario
     * POST /api/autenticacion/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Refrescar token
     * POST /api/autenticacion/refrescar
     */
    @PostMapping("/refrescar")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String newToken = authService.refreshToken(token);
            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);
            response.put("tipo", "Bearer");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token inválido o expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Validar token
     * GET /api/autenticacion/validar
     */
    @GetMapping("/validar")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            boolean isValid = authService.validateToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("valido", isValid);

            if (isValid) {
                String username = authService.getUsernameFromToken(token);
                response.put("username", username);
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Obtener usuario actual
     * GET /api/autenticacion/yo
     */
    @GetMapping("/yo")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            UserDTO user = authService.getCurrentUser(token);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo obtener el usuario");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}