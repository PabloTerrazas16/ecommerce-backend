package com.ecommerce.levelup.auth.service;

import com.ecommerce.levelup.auth.dto.LoginRequest;
import com.ecommerce.levelup.auth.dto.LoginResponse;
import com.ecommerce.levelup.auth.dto.RegisterRequest;
import com.ecommerce.levelup.security.JwtUtil;
import com.ecommerce.levelup.user.model.Role;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.RoleRepository;
import com.ecommerce.levelup.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Registrar nuevo usuario
     */
    public LoginResponse register(RegisterRequest request) {
        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
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
        user.setEnabled(true);

        // Asignar rol por defecto (ROLE_USER)
        Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Guardar usuario
        User savedUser = userRepository.save(user);

        // Generar token
        String token = jwtUtil.generateUserToken(savedUser.getUsername(), savedUser.getId());

        // Preparar respuesta
        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new LoginResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                roleNames
        );
    }

    /**
     * Login de usuario
     */
    public LoginResponse login(LoginRequest request) {
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Obtener datos del usuario
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verificar que el usuario esté habilitado
        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // Generar token
        String token = jwtUtil.generateUserToken(user.getUsername(), user.getId());

        // Preparar respuesta
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roleNames
        );
    }

    /**
     * Refrescar token
     */
    public LoginResponse refreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        String newToken = jwtUtil.generateUserToken(user.getUsername(), user.getId());

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new LoginResponse(
                newToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roleNames
        );
    }

    /**
     * Validar token
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token, JwtUtil.TokenType.USER);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtUtil.validateUserToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtener usuario actual desde token
     */
    public User getCurrentUser(String token) {
        String username = jwtUtil.extractUsername(token, JwtUtil.TokenType.USER);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}