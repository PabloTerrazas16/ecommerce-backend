package com.ecommerce.levelup.auth.service;

import com.ecommerce.levelup.auth.dto.LoginRequest;
import com.ecommerce.levelup.auth.dto.LoginResponse;
import com.ecommerce.levelup.auth.dto.RegisterRequest;
import com.ecommerce.levelup.security.JwtUtil;
import com.ecommerce.levelup.user.dto.UserDTO;
import com.ecommerce.levelup.user.model.Role;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.RoleRepository;
import com.ecommerce.levelup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Registrar nuevo usuario
     */
    @Transactional
    public void register(RegisterRequest request) {
        // Validar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validar si el username ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Validar longitud de contraseña
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRegion(request.getRegion());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Asignar rol USER por defecto
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
    }

    /**
     * Login de usuario
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("=== INICIO LOGIN ===");
        log.info("Username: {}", request.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtil.generateToken(authentication.getName());

        // Usar findByUsernameWithRoles para cargar roles con JOIN FETCH
        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        log.info("Usuario encontrado: {}", user.getUsername());
        log.info("User ID: {}", user.getId());
        log.info("Roles en el objeto User: {}", user.getRoles());
        log.info("Cantidad de roles: {}", user.getRoles().size());
        
        // Si no hay roles, asignar manualmente desde la base de datos
        Set<String> roles;
        if (user.getRoles().isEmpty()) {
            log.warn("⚠️ ROLES VACÍOS - Buscando roles directamente desde RoleRepository");
            
            // Buscar todos los roles y agregarlos al usuario
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN no encontrado"));
            
            user.getRoles().add(adminRole);
            userRepository.save(user);
            
            log.info("✅ Rol ROLE_ADMIN agregado al usuario admin");
            
            roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        } else {
            // Forzar inicialización de la colección de roles
            user.getRoles().forEach(role -> {
                log.info("Role ID: {}, Name: {}", role.getId(), role.getName());
            });

            // Extraer roles
            roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        }

        log.info("Roles extraídos: {}", roles);
        log.info("=== FIN LOGIN ===");

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
    /**
     * Refrescar token
     */
    public String refreshToken(String oldToken) {
        try {
            // Remover "Bearer " del token
            String token = oldToken.replace("Bearer ", "");

            // Validar token
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token inválido o expirado");
            }

            // Obtener username del token
            String username = jwtUtil.getUsernameFromToken(token);

            // Verificar que el usuario existe
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Generar nuevo token
            return jwtUtil.generateToken(username);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo refrescar el token: " + e.getMessage());
        }
    }

    /**
     * Validar token
     */
    public boolean validateToken(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            return jwtUtil.validateToken(cleanToken);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtener username desde el token
     */
    public String getUsernameFromToken(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            return jwtUtil.getUsernameFromToken(cleanToken);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el usuario del token");
        }
    }

    /**
     * Obtener usuario actual desde el token
     */
    public UserDTO getCurrentUser(String token) {
        try {
            String username = getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return convertToDTO(user);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el usuario actual: " + e.getMessage());
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setRegion(user.getRegion());
        dto.setCity(user.getCity());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}