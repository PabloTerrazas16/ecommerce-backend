package com.ecommerce.levelup.product.service;

import com.ecommerce.levelup.product.dto.ProductDTO;
import com.ecommerce.levelup.product.model.Category;
import com.ecommerce.levelup.product.model.Product;
import com.ecommerce.levelup.product.repository.CategoryRepository;
import com.ecommerce.levelup.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return convertToDTO(product);
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoryId));

        return productRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new RuntimeException("La consulta de búsqueda no puede estar vacía");
        }

        return productRepository.searchProducts(query.toLowerCase()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Validar categoría
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productDTO.getCategoryId()));

        // Validar SKU único
        if (productDTO.getSku() != null && productRepository.existsBySku(productDTO.getSku())) {
            throw new RuntimeException("Ya existe un producto con el SKU: " + productDTO.getSku());
        }

        // Validar precio
        if (productDTO.getPrice() == null || productDTO.getPrice().doubleValue() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        // Validar stock
        if (productDTO.getStock() == null || productDTO.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategory(category);
        product.setBrand(productDTO.getBrand());
        product.setSku(productDTO.getSku());
        product.setImageUrl(productDTO.getImageUrl());
        product.setWeight(productDTO.getWeight());
        product.setActive(productDTO.getActive() != null ? productDTO.getActive() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Validar categoría
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productDTO.getCategoryId()));

        // Validar SKU único (excepto para el mismo producto)
        if (productDTO.getSku() != null && 
            !productDTO.getSku().equals(product.getSku()) && 
            productRepository.existsBySku(productDTO.getSku())) {
            throw new RuntimeException("Ya existe un producto con el SKU: " + productDTO.getSku());
        }

        // Validar precio
        if (productDTO.getPrice() == null || productDTO.getPrice().doubleValue() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        // Validar stock
        if (productDTO.getStock() == null || productDTO.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategory(category);
        product.setBrand(productDTO.getBrand());
        product.setSku(productDTO.getSku());
        product.setImageUrl(productDTO.getImageUrl());
        product.setWeight(productDTO.getWeight());
        product.setActive(productDTO.getActive());
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (quantity < 0) {
            throw new RuntimeException("La cantidad de stock no puede ser negativa");
        }

        product.setStock(quantity);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        productRepository.delete(product);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setBrand(product.getBrand());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setWeight(product.getWeight());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}