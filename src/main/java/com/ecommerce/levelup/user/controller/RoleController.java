package com.ecommerce.levelup.user.controller;

import com.ecommerce.levelup.user.dto.CreateRoleRequest;
import com.ecommerce.levelup.user.dto.RoleDTO;
import com.ecommerce.levelup.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Roles", description = "Gestión de roles y permisos del sistema - Solo administradores")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Listar todos los roles", description = "Obtiene la lista completa de roles del sistema con contador de usuarios (requiere ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista de roles")
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

   
    @Operation(summary = "Obtener rol por ID", description = "Retorna la información de un rol específico (requiere ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol encontrado"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        try {
            RoleDTO role = roleService.getRoleById(id);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

  
    @Operation(summary = "Crear rol", description = "Crea un nuevo rol personalizado. Debe empezar con ROLE_ (requiere ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o rol ya existe")
    })
    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest request) {
        try {
            RoleDTO newRole = roleService.createRole(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

   
    @Operation(summary = "Actualizar rol", description = "Actualiza un rol personalizado. No permite modificar ROLE_ADMIN ni ROLE_USER (requiere ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede modificar rol del sistema")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoleRequest request) {
        try {
            RoleDTO updatedRole = roleService.updateRole(id, request);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

   
    @Operation(summary = "Eliminar rol", description = "Elimina un rol personalizado. No permite eliminar roles del sistema ni roles con usuarios asignados (requiere ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol eliminado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede eliminar rol del sistema o con usuarios asignados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Rol eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}/*ejemplo
@Tag(name = "", description = "")
@RestController
@RequestMapping("/api")
public class templateController {


    @Operation(
            summary = " frase (metodo)",
            description = "Endpoint que reciba algo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje procesado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado - Token inválido"),
            @ApiResponse(responseCode = "400", description = "Parámetro mensaje es requerido")
    })


    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> "/"Get(
            @Parameter(description = "Mensaje personalizado", required = true)
            @RequestParam String mensaje) {

        if (mensaje == null || mensaje.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El parámetro 'mensaje' es requerido");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "¡Hola! Recibí tu mensaje: " + mensaje);
        response.put("mensajeOriginal", mensaje);
        response.put("longitud", mensaje.length());
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

*/