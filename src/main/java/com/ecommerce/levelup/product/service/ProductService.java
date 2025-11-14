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

        // Generar código automáticamente si no se proporciona
        String sku = productDTO.getCode();
        if (sku == null || sku.isEmpty()) {
            sku = generateSku(category);
        }
        
        // Validar código único
        if (productRepository.existsByCode(sku)) {
            throw new RuntimeException("Ya existe un producto con el código: " + sku);
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
        product.setCode(sku);
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setCategory(category);
        product.setImage(productDTO.getImage());
        product.setDescription(productDTO.getDescription());
        product.setStock(productDTO.getStock());
        product.setFeatured(productDTO.getFeatured() != null ? productDTO.getFeatured() : false);
        product.setActive(productDTO.getActive() != null ? productDTO.getActive() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }
    
    /**
     * Generar código/SKU automáticamente basado en la categoría
     * Formato: CODIGO_CATEGORIA + NUMERO_SECUENCIAL (ej: JM001, AC002, CO001)
     */
    private String generateSku(Category category) {
        String prefix = category.getCode();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "PROD";
        }
        
        // Buscar el último código con este prefijo
        List<Product> productsInCategory = productRepository.findByCategory(category);
        int maxNumber = 0;
        
        for (Product p : productsInCategory) {
            if (p.getCode() != null && p.getCode().startsWith(prefix)) {
                try {
                    String numberPart = p.getCode().substring(prefix.length());
                    int number = Integer.parseInt(numberPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Ignorar códigos con formato inválido
                }
            }
        }
        
        // Generar siguiente número
        int nextNumber = maxNumber + 1;
        return String.format("%s%03d", prefix, nextNumber);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Validar categoría
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productDTO.getCategoryId()));

        // Validar código único (excepto para el mismo producto)
        if (productDTO.getCode() != null && 
            !productDTO.getCode().equals(product.getCode()) && 
            productRepository.existsByCode(productDTO.getCode())) {
            throw new RuntimeException("Ya existe un producto con el código: " + productDTO.getCode());
        }

        // Validar precio
        if (productDTO.getPrice() == null || productDTO.getPrice().doubleValue() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        // Validar stock
        if (productDTO.getStock() == null || productDTO.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        product.setCode(productDTO.getCode());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setCategory(category);
        product.setImage(productDTO.getImage());
        product.setDescription(productDTO.getDescription());
        product.setStock(productDTO.getStock());
        product.setFeatured(productDTO.getFeatured());
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
        dto.setCode(product.getCode());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategory(product.getCategory().getName());
        dto.setImage(product.getImage());
        dto.setDescription(product.getDescription());
        dto.setStock(product.getStock());
        dto.setFeatured(product.getFeatured());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}