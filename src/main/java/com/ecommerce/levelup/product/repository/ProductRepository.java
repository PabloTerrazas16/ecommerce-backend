package com.ecommerce.levelup.product.repository;

import com.ecommerce.levelup.product.model.Category;
import com.ecommerce.levelup.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar por categoría
    List<Product> findByCategory(Category category);

    // Buscar productos activos
    List<Product> findByActiveTrue();

    // Verificar si existe un código
    boolean existsByCode(String code);

    // Buscar productos por nombre o descripción (búsqueda)
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    // Buscar por categoría y activo
    List<Product> findByCategoryAndActiveTrue(Category category);

    // Buscar productos con stock bajo
    @Query("SELECT p FROM Product p WHERE p.stock < :minStock AND p.active = true")
    List<Product> findLowStockProducts(@Param("minStock") Integer minStock);
    
    // Buscar productos destacados
    List<Product> findByFeaturedTrueAndActiveTrue();
}