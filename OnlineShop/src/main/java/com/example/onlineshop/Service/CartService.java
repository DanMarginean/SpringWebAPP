package com.example.onlineshop.Service;

import com.example.onlineshop.Repository.OrderRepository;
import com.example.onlineshop.dto.CartItemRequestDto;
import com.example.onlineshop.dto.CartResponseDto;
import com.example.onlineshop.Entity.*;
import com.example.onlineshop.Util.CartMapper;
import com.example.onlineshop.Repository.CartRepository;
import com.example.onlineshop.Repository.CustomerRepository;
import com.example.onlineshop.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartMapper cartMapper;

    public CartResponseDto getCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createEmptyCart(customerId));
        return cartMapper.toResponse(cart);
    }

    public CartResponseDto addItem(Long customerId, CartItemRequestDto request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createEmptyCart(customerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // check if product already in cart
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    public CartResponseDto removeItem(Long customerId, Long productId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cart.setUpdatedAt(LocalDateTime.now());
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    public CartResponseDto updateItem(Long customerId, Long productId, int quantity) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            // remove item if quantity is 0
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    private Cart createEmptyCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Cart cart = Cart.builder()
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartRepository.save(cart);
    }

    @Transactional
    public Order checkout(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create new order
        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Convert cart items → order items
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .priceAtPurchase(cartItem.getProduct().getPrice())
                        .build())
                .toList();

        order.setItems(orderItems);

        // Compute total amount
        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }
}
