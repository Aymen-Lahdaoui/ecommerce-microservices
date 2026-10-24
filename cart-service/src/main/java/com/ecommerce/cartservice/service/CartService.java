package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(String userId, Cart.CartItem newItem) {
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

        boolean productExists = false;
        for (Cart.CartItem item : cart.getItems()) {
            if (item.getProductId() != null && item.getProductId().trim().equals(newItem.getProductId().trim())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
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