package com.ecommerce.notificationservice.event;

import com.ecommerce.notificationservice.config.RabbitMQConfig;
import com.ecommerce.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println(">>> [NOTIFICATION SERVICE] Réception de l'événement de commande #" + event.getOrderId());
        emailService.sendOrderConfirmation(event);
    }
}
