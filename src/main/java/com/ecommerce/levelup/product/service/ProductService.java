package com.ecommerce.levelup.product.service;

import com.ecommerce.levelup.product.dto.ProductDTO;
import com.ecommerce.levelup.product.dto.ProductQueryDTO;
import com.ecommerce.levelup.product.model.Category;
import com.ecommerce.levelup.product.model.Product;
import com.ecommerce.levelup.product.repository.CategoryRepository;
import com.ecommerce.levelup.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Crear nuevo producto
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Validar SKU único
        if (productDTO.getSku() != null && productRepository.existsBySku(productDTO.getSku())) {
            throw new RuntimeException("Product with SKU already exists: " + productDTO.getSku());
        }

        // Obtener categoría
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));

        // Crear producto
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setImageUrl(productDTO.getImageUrl());
        product.setActive(productDTO.getActive() != null ? productDTO.getActive() : true);
        product.setCategory(category);
        product.setBrand(productDTO.getBrand());
        product.setSku(productDTO.getSku());
        product.setWeight(productDTO.getWeight());

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    /**
     * Obtener todos los productos
     */
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos activos
     */
    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos con paginación
     */
    public Page<ProductDTO> getProductsPage(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);

        return productPage.map(this::convertToDTO);
    }

    /**
     * Obtener producto por ID
     */
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    /**
     * Obtener productos por categoría
     */
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos por categoría con paginación
     */
    public Page<ProductDTO> getProductsByCategoryPage(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return productPage.map(this::convertToDTO);
    }

    /**
     * Buscar productos por palabra clave
     */
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar productos con paginación
     */
    public Page<ProductDTO> searchProductsPage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);
        return productPage.map(this::convertToDTO);
    }

    /**
     * Obtener productos por marca
     */
    public List<ProductDTO> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos disponibles (con stock)
     */
    public List<ProductDTO> getAvailableProducts() {
        return productRepository.findAvailableProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtrar productos con múltiples criterios
     */
    public List<ProductDTO> filterProducts(ProductQueryDTO queryDTO) {
        List<Product> products = productRepository.findAll();

        // Aplicar filtros
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            products = productRepository.searchProducts(queryDTO.getKeyword());
        }

        if (queryDTO.getCategoryId() != null) {
            products = products.stream()
                    .filter(p -> p.getCategory().getId().equals(queryDTO.getCategoryId()))
                    .collect(Collectors.toList());
        }

        if (queryDTO.getMinPrice() != null && queryDTO.getMaxPrice() != null) {
            products = products.stream()
                    .filter(p -> p.getPrice().compareTo(queryDTO.getMinPrice()) >= 0
                            && p.getPrice().compareTo(queryDTO.getMaxPrice()) <= 0)
                    .collect(Collectors.toList());
        }

        if (queryDTO.getBrand() != null && !queryDTO.getBrand().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getBrand() != null && p.getBrand().equalsIgnoreCase(queryDTO.getBrand()))
                    .collect(Collectors.toList());
        }

        if (queryDTO.getActive() != null) {
            products = products.stream()
                    .filter(p -> p.getActive().equals(queryDTO.getActive()))
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar producto
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Actualizar campos
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }

        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStock() != null) {
            product.setStock(productDTO.getStock());
        }

        if (productDTO.getImageUrl() != null) {
            product.setImageUrl(productDTO.getImageUrl());
        }

        if (productDTO.getActive() != null) {
            product.setActive(productDTO.getActive());
        }

        if (productDTO.getCategoryId() != null && !productDTO.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (productDTO.getBrand() != null) {
            product.setBrand(productDTO.getBrand());
        }

        if (productDTO.getSku() != null && !productDTO.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(productDTO.getSku())) {
                throw new RuntimeException("Product with SKU already exists");
            }
            product.setSku(productDTO.getSku());
        }

        if (productDTO.getWeight() != null) {
            product.setWeight(productDTO.getWeight());
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Actualizar stock de producto
     */
    public ProductDTO updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setStock(quantity);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Incrementar stock
     */
    public ProductDTO increaseStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.increaseStock(quantity);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Decrementar stock
     */
    public ProductDTO decreaseStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.decreaseStock(quantity);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Activar/Desactivar producto
     */
    public ProductDTO toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setActive(!product.getActive());
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Eliminar producto
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Convertir Product a ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.getActive());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setBrand(product.getBrand());
        dto.setSku(product.getSku());
        dto.setWeight(product.getWeight());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setAvailable(product.isAvailable());
        return dto;
    }
}