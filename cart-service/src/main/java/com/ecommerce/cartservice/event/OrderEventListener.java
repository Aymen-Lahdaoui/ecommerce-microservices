package com.ecommerce.cartservice.event;

import com.ecommerce.cartservice.service.CartService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private CartService cartService;

    @RabbitListener(queues = "order-events-queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("Événement 'OrderCreatedEvent' reçu pour la commande : " + event.getOrderId());

        // Vider le panier de l'utilisateur de manière asynchrone
        cartService.clearCart(event.getUserId());

        System.out.println("Panier vidé avec succès pour l'utilisateur : " + event.getUserId());
    }
}