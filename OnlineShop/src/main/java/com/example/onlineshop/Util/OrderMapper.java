package com.example.onlineshop.Util;

import com.example.onlineshop.dto.OrderItemResponseDto;
import com.example.onlineshop.dto.OrderResponseDto;
import com.example.onlineshop.Entity.Order;
import com.example.onlineshop.Entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDto toResponse(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .build();
    }

    private OrderItemResponseDto toItemResponse(OrderItem item) {
        return OrderItemResponseDto.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }
}
