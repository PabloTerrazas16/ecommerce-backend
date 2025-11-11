package com.ecommerce.levelup.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQueryDTO {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Boolean active;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";
}