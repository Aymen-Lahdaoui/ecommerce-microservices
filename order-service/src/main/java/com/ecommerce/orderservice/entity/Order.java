package com.ecommerce.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;
    private String userId;
    private Object items; // Contient la liste des produits récupérés du panier
    private double totalPrice;
    private LocalDateTime orderDate;
    private String status; // "PAID", "PAYMENT_FAILED", "CANCELLED"
    private String paymentTransactionId;
}