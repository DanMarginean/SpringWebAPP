package com.example.onlineshop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponseDto {
    private Long productId;
    private String productName;
    private int quantity;
}