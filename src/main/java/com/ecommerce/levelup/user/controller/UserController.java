package com.ecommerce.levelup.user.controller;

import com.ecommerce.levelup.user.dto.UserDTO;
import com.ecommerce.levelup.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /*
      Actualizar usuario (Solo ADMIN o el mismo usuario)
      PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id)")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO) {
        try {
            UserDTO updated = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        try {
            userService.toggleUserStatus(id, activo);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", activo ? "Usuario activado" : "Usuario desactivado");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
/*ejemplo de post

    @Operation(
            summary = " (POST)",
            description = "Endpoint privado que recibe un mensaje en el body y devuelve respuesta"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje procesado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado - Token inválido"),
            @ApiResponse(responseCode = "400", description = "Body inválido o mensaje vacío")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/holamundo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> holaMundoPost(
            @Parameter(description = "Objeto con el mensaje", required = true)
            @RequestBody Map<String, String> body) {

        String mensaje = body.get("mensaje");

        if (mensaje == null || mensaje.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "El campo 'mensaje' en el body es requerido");
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "¡Hola desde POST! Tu mensaje fue: " + mensaje);
        response.put("mensajeOriginal", mensaje);
        response.put("mensajeMayusculas", mensaje.toUpperCase());
        response.put("mensajeMinusculas", mensaje.toLowerCase());
        response.put("longitud", mensaje.length());
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }


 */