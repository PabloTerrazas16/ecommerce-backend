package com.ecommerce.levelup.product.controller;

import com.ecommerce.levelup.product.dto.ProductDTO;
import com.ecommerce.levelup.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

  
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoriaId));
    }

  
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String consulta) {
        return ResponseEntity.ok(productService.searchProducts(consulta));
    }

   
    @GetMapping("/activos")
    public ResponseEntity<List<ProductDTO>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO created = productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

 
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updated = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        try {
            productService.updateStock(id, cantidad);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Stock actualizado exitosamente");
            response.put("nuevo_stock", cantidad.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

   
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Producto eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}