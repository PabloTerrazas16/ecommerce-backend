package com.ecommerce.levelup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins:http://localhost:5173,http://localhost:3000,https://discord.com/api/webhooks/1440134208566726656/SnV2E09Jeknk2uNF7gPS__XzJRiZ2qA5xBownhA9C-qCJqwT7p4Fwf_DXAXYGGCKhntu}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // split and trim origins, ignore empty entries
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(s -> s != null && !s.isEmpty())
            .toList();
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        configuration.setAllowedHeaders(Arrays.asList("*"));

        // allow credentials if frontend needs to send cookies/auth headers
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}