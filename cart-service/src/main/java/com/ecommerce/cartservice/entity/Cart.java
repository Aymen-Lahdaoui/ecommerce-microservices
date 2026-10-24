package com.ecommerce.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    @Id
    private String id;
    private String userId;
    private List<CartItem> items = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItem {
        private String productId;
        private String productName;
        private double price;
        private int quantity;
    }
}