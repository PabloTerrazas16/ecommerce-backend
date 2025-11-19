package com.ecommerce.levelup.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Backend API")
                        .version("1.0.0")
                        .description("""
                                API RESTful completa para sistema de e-commerce desarrollado con Spring Boot 3.2.0 y Java 17.
                                
                                ## Características:
                                - Autenticación JWT
                                - Gestión de usuarios y roles
                                - Catálogo de productos y categorías
                                - Sistema de pagos
                                - Auditoría completa con AOP
                                - Seguridad multi-capa
                                
                                ## Autenticación:
                                Para usar endpoints protegidos, primero debes:
                                1. Hacer login en `/autenticacion/login`
                                2. Copiar el token recibido
                                3. Hacer clic en el botón "Authorize"
                                4. Pegar el token (solo el token, sin "Bearer")
                                
                                ## Credenciales de prueba:
                                - **Admin:** username=`admin`, password=`admin123`
                                - **User:** username=`user`, password=`user123`
                                """)
                        .contact(new Contact()
                                .name("E-Commerce Team")
                                .email("admin@ecommerce.com")
                                .url("https://github.com/PabloTerrazas16/ecommerce-backend"))
                        .license(new License()
                                .name("Proyecto Educativo")
                                .url("https://github.com/PabloTerrazas16/ecommerce-backend")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo Local")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido del endpoint /autenticacion/login")));
    }
}
