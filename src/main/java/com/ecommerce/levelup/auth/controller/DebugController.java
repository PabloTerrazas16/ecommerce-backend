package com.ecommerce.levelup.auth.controller;

import com.ecommerce.levelup.user.model.Role;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.RoleRepository;
import com.ecommerce.levelup.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Tag(name = "Debug", description = "Herramientas de desarrollo para resetear y consultar usuarios - Solo perfil dev")
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")  
public class DebugController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Resetear admin", description = "Restaura la contrase\u00f1a del usuario admin a admin123 y asegura que tenga ROLE_ADMIN")
    @ApiResponse(responseCode = "200", description = "Admin reseteado con \u00e9xito")
    @PostMapping("/reset-admin")
    @Transactional
    public ResponseEntity<?> resetAdmin() {
        try {
            User admin = userRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

            admin.setPassword(passwordEncoder.encode("admin123"));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN no encontrado"));

            admin.getRoles().clear();
            admin.getRoles().add(adminRole);

            User savedAdmin = userRepository.save(admin);

            log.info("Admin reseteado exitosamente");
            log.info("Username: admin");
            log.info("Password: admin123");
            log.info("Roles: {}", savedAdmin.getRoles());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin reseteado exitosamente");
            response.put("username", "admin");
            response.put("password", "admin123");
            response.put("roles", savedAdmin.getRoles().stream().map(Role::getName).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al resetear admin: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Info del admin", description = "Obtiene informaci\u00f3n detallada del usuario admin incluyendo roles")
    @ApiResponse(responseCode = "200", description = "Informaci\u00f3n del admin")
    @GetMapping("/admin-info")
    @Transactional
    public ResponseEntity<?> getAdminInfo() {
        try {
            User admin = userRepository.findByUsernameWithRoles("admin")
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

            Map<String, Object> info = new HashMap<>();
            info.put("id", admin.getId());
            info.put("username", admin.getUsername());
            info.put("email", admin.getEmail());
            info.put("enabled", admin.getEnabled());
            info.put("rolesCount", admin.getRoles().size());
            info.put("roles", admin.getRoles().stream().map(Role::getName).toList());

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Todos los usuarios", description = "Lista todos los usuarios del sistema con sus roles")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @GetMapping("/all-users")
    @Transactional
    public ResponseEntity<?> getAllUsers() {
        var users = userRepository.findAll();
        
        var result = users.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("rolesCount", user.getRoles().size());
            userInfo.put("roles", user.getRoles().stream().map(Role::getName).toList());
            return userInfo;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
