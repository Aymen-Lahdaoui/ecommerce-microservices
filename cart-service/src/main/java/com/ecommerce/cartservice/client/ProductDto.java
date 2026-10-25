package com.ecommerce.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String id;
    private String name;
    private String description;
    private double price;
    private int stock;

    // Propriété calculée ou renvoyée pour indiquer l'état du stock
    public boolean isOutOfStock() {
        return this.stock <= 0;
    }
}