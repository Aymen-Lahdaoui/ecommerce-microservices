package com.ecommerce.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable; // 1. Ajoute cet import

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem implements Serializable { // 2. Ajoute implements Serializable ici
    private String productId;
    private String productName;
    private int quantity;
    private double price;
}