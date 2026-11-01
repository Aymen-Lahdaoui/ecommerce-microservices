package com.ecommerce.cartservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE    = "order-events-exchange";
    public static final String CART_QUEUE        = "order-events-queue";
    public static final String CART_ROUTING_KEY  = "order.created";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

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
