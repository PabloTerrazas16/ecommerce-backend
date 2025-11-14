package com.ecommerce.levelup.product.service;

import com.ecommerce.levelup.product.dto.CategoryDTO;
import com.ecommerce.levelup.product.model.Category;
import com.ecommerce.levelup.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Crear nueva categoría
     */
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Validar que el nombre no exista
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category with name already exists: " + categoryDTO.getName());
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        
        // Generar código automáticamente si no se proporciona
        if (categoryDTO.getCode() == null || categoryDTO.getCode().isEmpty()) {
            category.setCode(generateCategoryCode(categoryDTO.getName()));
        } else {
            category.setCode(categoryDTO.getCode().toUpperCase());
        }
        
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());
        category.setActive(categoryDTO.getActive() != null ? categoryDTO.getActive() : true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    /**
     * Generar código de categoría basado en el nombre
     * Ej: "Juegos de Mesa" -> "JM", "Accesorios" -> "AC"
     */
    private String generateCategoryCode(String categoryName) {
        // Remover artículos y palabras comunes
        String cleaned = categoryName.toUpperCase()
                .replace("DE ", "")
                .replace("LA ", "")
                .replace("EL ", "")
                .replace("LOS ", "")
                .replace("LAS ", "");
        
        String[] words = cleaned.split("\\s+");
        
        // Si solo hay una palabra, tomar las primeras 2 letras
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length()));
        }
        
        // Si hay múltiples palabras, tomar la primera letra de cada palabra
        StringBuilder code = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                code.append(word.charAt(0));
            }
        }
        
        return code.toString();
    }

    /**
     * Obtener todas las categorías
     */
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener categoría por ID
     */
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    /**
     * Obtener categorías activas
     */
    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar categorías
     */
    public List<CategoryDTO> searchCategories(String keyword) {
        return categoryRepository.searchCategories(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar categoría
     */
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Actualizar campos
        if (categoryDTO.getName() != null && !categoryDTO.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(categoryDTO.getName())) {
                throw new RuntimeException("Category with name already exists");
            }
            category.setName(categoryDTO.getName());
        }

        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }

        if (categoryDTO.getImageUrl() != null) {
            category.setImageUrl(categoryDTO.getImageUrl());
        }

        if (categoryDTO.getActive() != null) {
            category.setActive(categoryDTO.getActive());
        }
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    /**
     * Activar/Desactivar categoría
     */
    public CategoryDTO toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setActive(!category.getActive());
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    /**
     * Eliminar categoría
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Verificar que no tenga productos asociados
        if (!category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
    }

    /**
     * Convertir Category a CategoryDTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCode(category.getCode());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setActive(category.getActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}