package com.ecommerce.levelup.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String code; 

    private String description;

    private String imageUrl;

    private Boolean active;

    private Integer productCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}