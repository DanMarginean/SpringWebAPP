package com.example.onlineshop.Controller;

import com.example.onlineshop.Entity.OrderStatus;
import com.example.onlineshop.dto.OrderRequestDto;
import com.example.onlineshop.dto.OrderResponseDto;
import com.example.onlineshop.Service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "List all orders", description = "Returns every order in the system. Admin only.")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order for a customer, including order items",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order successfully created",
                            content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer or Product not found",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto request
    ) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Fetch a single order by its ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order found",
                            content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    public ResponseEntity<OrderResponseDto> getOrderById(
            @Parameter(description = "ID of the order to fetch") @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "Get all orders for a customer",
            description = "Returns a list of all orders placed by a given customer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Orders found",
                            content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public ResponseEntity<List<OrderResponseDto>> getOrdersByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update order status",
            description = "Updates the status of an existing order"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @Parameter(description = "Order ID", example = "42")
            @PathVariable Long id,

            @Parameter(
                    description = "New status value (allowed: PENDING, PAID, SHIPPED, DELIVERED, CANCELLED)",
                    example = "SHIPPED"
            )
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
