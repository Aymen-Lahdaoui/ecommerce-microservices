package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCart(
            @RequestHeader("X-Auth-User") String userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {

        // Bloquer l'admin s'il essaie d'accéder au panier
        if ("ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Les administrateurs n'ont pas accès au panier.");
        }

        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestHeader("X-Auth-User") String userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role,
            @RequestBody Cart.CartItem item) {

        // Empêcher un ADMIN d'ajouter des articles au panier
        if ("ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Les administrateurs ne peuvent pas ajouter d'articles au panier.");
        }

        return ResponseEntity.ok(cartService.addToCart(userId, item));
    }

    @DeleteMapping("/item/{productId}")
    public ResponseEntity<?> removeItemFromCart(
            @RequestHeader("X-Auth-User") String userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role,
            @PathVariable String productId) {

        // Empêcher un ADMIN de modifier un panier
        if ("ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Les administrateurs ne peuvent pas modifier de panier.");
        }

        return ResponseEntity.ok(cartService.removeItemFromCart(userId, productId));
    }

    @PutMapping("/item/{productId}/decrease")
    public ResponseEntity<?> decreaseQuantity(
            @RequestHeader("X-Auth-User") String userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role,
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int quantity) {

        // Empêcher un ADMIN de modifier un panier
        if ("ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Les administrateurs ne peuvent pas modifier de panier.");
        }

        return ResponseEntity.ok(cartService.decreaseItemQuantity(userId, productId, quantity));
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(
            @RequestHeader("X-Auth-User") String userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {

        // Empêcher un ADMIN de modifier un panier
        if ("ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Les administrateurs ne peuvent pas modifier de panier.");
        }

        cartService.clearCart(userId);
        return ResponseEntity.ok("Panier vidé avec succès.");
    }
}