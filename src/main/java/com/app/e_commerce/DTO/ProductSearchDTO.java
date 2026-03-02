package com.app.e_commerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchDTO {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private Double averageRating;
}
