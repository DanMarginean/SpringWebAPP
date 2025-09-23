package com.example.onlineshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponseDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtPurchase;
}
