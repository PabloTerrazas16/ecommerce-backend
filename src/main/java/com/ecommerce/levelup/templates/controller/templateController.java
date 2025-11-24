package com.ecommerce.levelup.templates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/*ejemplo de verificacion


    @Operation(
            summary = "verificacion",
            description = "verificacion jwt"
    )
    @ApiResponse(responseCode = "200", description = "Token válido")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/verificar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> verificar() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Token JWT válido - Autenticación exitosa");
        response.put("status", "authenticated");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}

 */