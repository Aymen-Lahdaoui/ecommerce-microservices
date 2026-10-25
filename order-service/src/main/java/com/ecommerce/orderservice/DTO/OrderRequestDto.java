package com.ecommerce.orderservice.dto;

import lombok.Data;

@Data
public class OrderRequestDto {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private String paymentMethod; // ex: "CREDIT_CARD", "PAYPAL"
}