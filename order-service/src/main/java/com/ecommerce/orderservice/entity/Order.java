package com.ecommerce.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;
    private String userId;
    private List<OrderItem> items; // Liste typée des articles commandés
    private double totalPrice;
    private LocalDateTime orderDate;
    private String status; // "PENDING", "PAID", "PAYMENT_FAILED", "CANCELLED"
    private String paymentTransactionId;
}