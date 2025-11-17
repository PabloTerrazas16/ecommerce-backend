package com.ecommerce.levelup.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    @NotBlank(message = "El nombre del rol es requerido")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "El nombre debe empezar con ROLE_ y solo contener mayúsculas y guiones bajos")
    private String name;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String description;
}
