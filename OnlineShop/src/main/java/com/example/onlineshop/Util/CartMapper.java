package com.example.onlineshop.Util;

import com.example.onlineshop.dto.CartItemResponseDto;
import com.example.onlineshop.dto.CartResponseDto;
import com.example.onlineshop.Entity.Cart;
import com.example.onlineshop.Entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponseDto toResponse(Cart cart) {
        return CartResponseDto.builder()
                .id(cart.getId())
                .customerId(cart.getCustomer().getId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(cart.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private CartItemResponseDto toItemResponse(CartItem item) {
        return CartItemResponseDto.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .build();
    }
}