package com.ecommerce.levelup.config;

import com.ecommerce.levelup.user.model.Role;
import com.ecommerce.levelup.user.model.User;
import com.ecommerce.levelup.user.repository.RoleRepository;
import com.ecommerce.levelup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeUsers();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            log.info("Initializing roles...");

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator role with full access");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Standard user role");
            roleRepository.save(userRole);

            Role moderatorRole = new Role();
            moderatorRole.setName("ROLE_MODERATOR");
            moderatorRole.setDescription("Moderator role with limited admin access");
            roleRepository.save(moderatorRole);

            log.info("Roles initialized successfully!");
        }
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            log.info("Initializing users...");

            // Admin user
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);

            userRepository.save(admin);

            // Regular user
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("User role not found"));

            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setEmail("user@ecommerce.com");
            regularUser.setPassword(passwordEncoder.encode("user123"));
            regularUser.setFirstName("Regular");
            regularUser.setLastName("User");
            regularUser.setEnabled(true);
            regularUser.setCreatedAt(LocalDateTime.now());
            regularUser.setUpdatedAt(LocalDateTime.now());

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            regularUser.setRoles(userRoles);

            userRepository.save(regularUser);

            log.info("Users initialized successfully!");
            log.info("Admin credentials - Username: admin, Password: admin123");
            log.info("User credentials - Username: user, Password: user123");
        }
    }
}