package com.ecommerce.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/api/cart")
    Object getCart(@RequestHeader("X-Auth-User") String userId);

    @DeleteMapping("/api/cart")
    void clearCart(@RequestHeader("X-Auth-User") String userId);
}