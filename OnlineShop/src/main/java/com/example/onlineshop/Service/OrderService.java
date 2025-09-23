package com.example.onlineshop.Service;

import com.example.onlineshop.dto.OrderItemRequestDto;
import com.example.onlineshop.dto.OrderRequestDto;
import com.example.onlineshop.dto.OrderResponseDto;
import com.example.onlineshop.Entity.*;
import com.example.onlineshop.Util.OrderMapper;
import com.example.onlineshop.Repository.CustomerRepository;
import com.example.onlineshop.Repository.OrderRepository;
import com.example.onlineshop.Repository.ProductRepository;
import com.example.onlineshop.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper mapper;

    public OrderResponseDto createOrder(OrderRequestDto request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ProductNotFoundException("Customer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPriceAtPurchase((product.getPrice()));

            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return mapper.toResponse(saved);
    }

    public OrderResponseDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<OrderResponseDto> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public OrderResponseDto updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        return mapper.toResponse(orderRepository.save(order));
    }
}
