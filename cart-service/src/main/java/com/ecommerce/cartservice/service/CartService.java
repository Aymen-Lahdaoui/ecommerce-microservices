package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductClient;
import com.ecommerce.cartservice.dto.ProductDto;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.repository.CartRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient; // 👈 Injecté pour interroger le product-service

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
    }

    // ── Ajout avec Circuit Breaker & Retry ────────────────────────────────────
    @CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallback")
    @Retry(name = "productService")
    public Cart addToCart(String userId, Cart.CartItem newItem) {
        // 1. Récupérer le produit et son stock via OpenFeign
        ProductDto product = productClient.getProductById(newItem.getProductId());

        if (product == null) {
            throw new RuntimeException("Produit introuvable.");
        }

        // 2. Vérifier si le produit est en rupture de stock ("Out of stock")
        if (product.getStock() <= 0) {
            throw new RuntimeException("Out of stock : Impossible d'ajouter ce produit car il est en rupture de stock.");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return newCart;
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Calculer la quantité totale demandée (panier actuel + nouvel ajout)
        int existingQuantityInCart = 0;
        for (Cart.CartItem item : cart.getItems()) {
            if (item.getProductId() != null && item.getProductId().trim().equals(newItem.getProductId().trim())) {
                existingQuantityInCart = item.getQuantity();
                break;
            }
        }

        int totalRequestedQuantity = existingQuantityInCart + newItem.getQuantity();

        // 3. Vérifier si la quantité totale demandée dépasse le stock disponible
        if (product.getStock() < totalRequestedQuantity) {
            throw new RuntimeException("Stock insuffisant ! Stock disponible : " + product.getStock());
        }

        boolean productExists = false;
        for (Cart.CartItem item : cart.getItems()) {
            if (item.getProductId() != null && item.getProductId().trim().equals(newItem.getProductId().trim())) {
                item.setQuantity(totalRequestedQuantity);
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    /**
     * Méthode de Fallback exécutée automatiquement par Resilience4j quand :
     * - product-service est éteint ou ne répond pas
     * - le circuit breaker passe en statut OUVERT (Open State)
     */
    public Cart addToCartFallback(String userId, Cart.CartItem newItem, Throwable throwable) {
        System.err.println(">>> [CIRCUIT BREAKER - cart-service] Échec d'accès au product-service : " + throwable.getMessage());
        throw new RuntimeException("Le service catalogue est temporairement indisponible. Impossible de vérifier le produit. Veuillez réessayer dans quelques instants.");
    }

    // 1. SUPPRESSION TOTALE DE L'ARTICLE
    public Cart removeItemFromCart(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return createEmptyCart(userId);
        }

        if (cart.getItems() != null && productId != null) {
            String cleanProductId = productId.trim();
            cart.getItems().removeIf(item ->
                    item.getProductId() != null && item.getProductId().trim().equals(cleanProductId)
            );
        }

        return saveOrDeleteCart(cart, userId);
    }

    // 2. DIMINUTION DE LA QUANTITÉ (OU SUPPRESSION SI QUANTITÉ <= 0)
    public Cart decreaseItemQuantity(String userId, String productId, int quantityToReduce) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return createEmptyCart(userId);
        }

        if (cart.getItems() != null && productId != null) {
            String cleanProductId = productId.trim();

            cart.getItems().removeIf(item -> {
                if (item.getProductId() != null && item.getProductId().trim().equals(cleanProductId)) {
                    int newQuantity = item.getQuantity() - quantityToReduce;
                    if (newQuantity <= 0) {
                        return true; // Supprime l'item de la liste si quantité <= 0
                    } else {
                        item.setQuantity(newQuantity); // Diminue la quantité
                        return false;
                    }
                }
                return false;
            });
        }

        return saveOrDeleteCart(cart, userId);
    }

    // 3. VIDAGE TOTAL DU PANIER
    public void clearCart(String userId) {
        System.out.println(">>> CartService: Demande de vidage du panier pour userId : [" + userId + "]");
        cartRepository.findByUserId(userId).ifPresentOrElse(cart -> {
            if (cart.getItems() != null) {
                cart.getItems().clear();
            } else {
                cart.setItems(new ArrayList<>());
            }
            cartRepository.save(cart);
            cartRepository.delete(cart);
            System.out.println(">>> CartService: Panier vidé et supprimé de MongoDB avec succès pour userId : [" + userId + "]");
        }, () -> {
            System.out.println(">>> CartService: Aucun panier trouvé dans MongoDB pour userId : [" + userId + "]");
        });
    }

    // Méthode utilitaire pour effacer le document MongoDB si le panier est vide
    private Cart saveOrDeleteCart(Cart cart, String userId) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            cartRepository.delete(cart); // Supprime le document dans MongoDB
            return createEmptyCart(userId);
        }
        return cartRepository.save(cart);
    }

    private Cart createEmptyCart(String userId) {
        Cart emptyCart = new Cart();
        emptyCart.setUserId(userId);
        emptyCart.setItems(new ArrayList<>());
        return emptyCart;
    }
}
