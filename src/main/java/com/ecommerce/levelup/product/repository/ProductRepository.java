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

    List<Product> findByCategory(Category category);

    List<Product> findByActiveTrue();

    boolean existsByCode(String code);

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    List<Product> findByCategoryAndActiveTrue(Category category);

    @Query("SELECT p FROM Product p WHERE p.stock < :minStock AND p.active = true")
    List<Product> findLowStockProducts(@Param("minStock") Integer minStock);
    
    List<Product> findByFeaturedTrueAndActiveTrue();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int decreaseStockIfAvailable(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("qty") Integer qty);
}