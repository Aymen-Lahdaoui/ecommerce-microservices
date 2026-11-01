package com.ecommerce.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange unique partagé par tous les services ──────────────────────
    public static final String ORDER_EXCHANGE     = "order-events-exchange";

    // ── Queue écoutée par product-service pour réduire le stock ────────────
    public static final String PRODUCT_QUEUE      = "product-events-queue";
    public static final String PRODUCT_ROUTING_KEY = "order.created";

    // ── Queue écoutée par cart-service pour vider le panier (via RabbitMQ) ─
    public static final String CART_QUEUE         = "order-events-queue";
    public static final String CART_ROUTING_KEY   = "order.created";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    // Queue product-service
    @Bean
    public Queue productQueue() {
        return new Queue(PRODUCT_QUEUE, true);
    }

    @Bean
    public Binding productBinding(Queue productQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(productQueue)
                .to(orderExchange)
                .with(PRODUCT_ROUTING_KEY);
    }

    // Queue cart-service
    @Bean
    public Queue cartQueue() {
        return new Queue(CART_QUEUE, true);
    }

    @Bean
    public Binding cartBinding(Queue cartQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(cartQueue)
                .to(orderExchange)
                .with(CART_ROUTING_KEY);
    }

}
