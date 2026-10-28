package com.ecommerce.orderservice.event;

import com.ecommerce.orderservice.entity.OrderItem;
import java.io.Serializable;
import java.util.List;

public class OrderCreatedEvent implements Serializable {
    private String orderId;
    private String userId;
    private List<OrderItem> items;

    // Constructeur vide (néentiel pour la sérialisation JSON/AMQP)
    public OrderCreatedEvent() {
    }

    // Constructeur avec paramètres
    public OrderCreatedEvent(String orderId, String userId, List<OrderItem> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
    }

    // Getters et Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}