package com.ecommerce.levelup.templates;

/*


 /*
    ejemplo de Swagger GET

    @Tag(name="algo", description="algo")



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
    @GetMapping("/algo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> algoGet(
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


ejemplo Swagger POST





    @Operation(
            summary = "algo con (POST)",
            description = "Endpoint de post"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje procesado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado - Token inválido"),
            @ApiResponse(responseCode = "400", description = "Body inválido o mensaje vacío")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/algo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> algoPost(
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













