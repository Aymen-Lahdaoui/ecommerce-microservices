package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.event.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ecommerce.com}")
    private String fromEmail;

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        double total = 0.0;
        StringBuilder itemsSummary = new StringBuilder();

        if (event.getItems() != null) {
            for (OrderCreatedEvent.OrderItemDto item : event.getItems()) {
                double subTotal = item.getPrice() * item.getQuantity();
                total += subTotal;
                itemsSummary.append(String.format("   • Produit [%s] x %d : %.2f €\n",
                        item.getProductId(), item.getQuantity(), subTotal));
            }
        }

        String messageContent = String.format(
                "Bonjour %s,\n\n" +
                "Votre commande n° %s a été confirmée et payée avec succès !\n\n" +
                "Détail de votre commande :\n%s\n" +
                "Montant Total : %.2f €\n\n" +
                "Merci pour votre confiance,\nL'équipe E-Commerce.",
                event.getUserId(), event.getOrderId(), itemsSummary, total
        );

        // 1. Affichage dans la console
        System.out.println("\n================================================================================");
        System.out.println("📧 [NOTIFICATION - NOUVEL E-MAIL ENVOYÉ]");
        System.out.println("Destinataire : " + event.getUserId());
        System.out.println("Objet        : Confirmation de commande #" + event.getOrderId());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(messageContent);
        System.out.println("================================================================================\n");

        // 2. Envoi réel par SMTP si un serveur mail est configuré
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(event.getUserId().contains("@") ? event.getUserId() : fromEmail);
                message.setSubject("Confirmation de votre commande #" + event.getOrderId());
                message.setText(messageContent);
                mailSender.send(message);
                System.out.println(">>> E-mail SMTP expédié avec succès.");
            } catch (Exception e) {
                System.err.println(">>> [INFO] Serveur SMTP non connecté ou simulé : " + e.getMessage());
            }
        }
    }
}
