package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequestDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderHistory;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestHeader("X-Auth-User") String userId,
            @RequestBody OrderRequestDto orderRequest) {

        Order order = orderService.createOrderAndPay(userId, orderRequest);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderHistory>> getUserOrderHistory(
            @RequestHeader("X-Auth-User") String userId) {

        List<OrderHistory> successfulOrders = orderService.getUserSuccessfulOrders(userId);
        return ResponseEntity.ok(successfulOrders);
    }
}