package com.ecommerce.cartservice.event;

import com.ecommerce.cartservice.dto.OrderItemDto;
import java.io.Serializable;
import java.util.List;

public class OrderCreatedEvent implements Serializable {
    private String orderId;
    private String userId;
    private List<OrderItemDto> items;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String orderId, String userId, List<OrderItemDto> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
    }

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

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}