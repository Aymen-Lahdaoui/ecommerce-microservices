package com.ecommerce.orderservice.event;

import java.io.Serializable;
import java.util.List;

/**
 * Événement publié sur RabbitMQ après une commande payée.
 * Les items utilisent un DTO interne allégé (productId + quantity + price)
 * compatible avec la désérialisation dans product-service et cart-service.
 */
public class OrderCreatedEvent implements Serializable {

    private String orderId;
    private String userId;
    private List<OrderItemDto> items;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String orderId, String userId, List<OrderItemDto> items) {
        this.orderId = orderId;
        this.userId  = userId;
        this.items   = items;
    }

    // Getters / Setters
    public String getOrderId()                  { return orderId; }
    public void   setOrderId(String orderId)    { this.orderId = orderId; }

    public String getUserId()                   { return userId; }
    public void   setUserId(String userId)      { this.userId = userId; }

    public List<OrderItemDto> getItems()              { return items; }
    public void               setItems(List<OrderItemDto> items) { this.items = items; }

    // ── DTO interne ──────────────────────────────────────────────────────────
    public static class OrderItemDto implements Serializable {
        private String productId;
        private int    quantity;
        private double price;

        public OrderItemDto() {}

        public OrderItemDto(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity  = quantity;
            this.price     = price;
        }

        public String getProductId()                { return productId; }
        public void   setProductId(String productId){ this.productId = productId; }

        public int  getQuantity()                   { return quantity; }
        public void setQuantity(int quantity)       { this.quantity = quantity; }

        public double getPrice()                    { return price; }
        public void   setPrice(double price)        { this.price = price; }
    }
}
