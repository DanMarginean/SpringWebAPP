package com.example.onlineshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Order must contain items")
    private List<OrderItemRequestDto> items;
}
