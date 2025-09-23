package com.example.onlineshop.Controller;

import com.example.onlineshop.Entity.Order;
import com.example.onlineshop.Util.OrderMapper;
import com.example.onlineshop.dto.CartItemRequestDto;
import com.example.onlineshop.dto.CartResponseDto;
import com.example.onlineshop.Service.CartService;
import com.example.onlineshop.dto.OrderResponseDto;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {

    private final CartService cartService;
    private final OrderMapper orderMapper;

    @GetMapping("/{customerId}")
    @Operation(
            summary = "Get customer cart",
            description = "Fetches the shopping cart for a given customer. If the cart does not exist, a new empty one will be created.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CartResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public ResponseEntity<CartResponseDto> getCart(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/{customerId}/items")
    @Operation(
            summary = "Add item to cart",
            description = "Adds a product to the customer's cart. If the product already exists, the quantity will be increased.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item added to cart successfully",
                            content = @Content(schema = @Schema(implementation = CartResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request (e.g. missing productId or invalid quantity)"),
                    @ApiResponse(responseCode = "404", description = "Customer or Product not found")
            }
    )
    public ResponseEntity<CartResponseDto> addItem(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable Long customerId,
            @Valid @RequestBody CartItemRequestDto request) {
        return ResponseEntity.ok(cartService.addItem(customerId, request));
    }

    @PutMapping("/{customerId}/items/{productId}")
    @Operation(
            summary = "Update item quantity in cart",
            description = "Updates the quantity of a product in the cart. If quantity is set to 0, the item will be removed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart updated successfully",
                            content = @Content(schema = @Schema(implementation = CartResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Cart or Product not found")
            }
    )
    public ResponseEntity<CartResponseDto> updateItem(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable Long customerId,
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId,
            @Parameter(description = "New quantity for the product", required = true, example = "3")
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItem(customerId, productId, quantity));
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    @Operation(
            summary = "Remove item from cart",
            description = "Removes a specific product from the customer's cart.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item removed successfully",
                            content = @Content(schema = @Schema(implementation = CartResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Cart or Product not found")
            }
    )
    public ResponseEntity<CartResponseDto> removeItem(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable Long customerId,
            @Parameter(description = "ID of the product to remove", required = true)
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(customerId, productId));
    }

    @PostMapping("/{cartId}/checkout")
    @Operation(
            summary = "Checkout cart",
            description = "Converts the cart into an order and clears the cart"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checkout successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Cart is empty or invalid"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<OrderResponseDto> checkout(
            @Parameter(description = "ID of the cart to checkout", example = "1")
            @PathVariable Long cartId) {
        System.out.println(">>> Checkout called for cartId = " + cartId);
        Order order = cartService.checkout(cartId);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }
}