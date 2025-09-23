package com.example.onlineshop.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CartResponseDto {
    private Long id;
    private Long customerId;
    private List<CartItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}