package com.ecommerce.levelup.user.security;

import com.ecommerce.levelup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;

    /**
     * Comprueba si el usuario autenticado es el propietario del recurso (por id).
     */
    public boolean isOwner(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .map(user -> user.getId() != null && user.getId().equals(id))
                .orElse(false);
    }
}
