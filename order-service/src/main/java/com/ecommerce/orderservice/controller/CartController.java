package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.CartItemRequest;
import com.ecommerce.orderservice.dto.CartResponse;
import com.ecommerce.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addItemToCart(@PathVariable Long userId,
                                           @Valid @RequestBody CartItemRequest request) {
        try {
            log.info("Add to cart request - User: {}, Product: {}", userId, request.getProductId());
            CartResponse response = cartService.addItemToCart(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error adding item to cart: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Long userId) {
        try {
            CartResponse response = cartService.getCart(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching cart: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long userId,
                                            @PathVariable Long itemId,
                                            @RequestParam Integer quantity) {
        try {
            log.info("Update cart item - User: {}, Item: {}, Quantity: {}", userId, itemId, quantity);
            CartResponse response = cartService.updateCartItem(userId, itemId, quantity);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long userId,
                                                @PathVariable Long itemId) {
        try {
            log.info("Remove from cart - User: {}, Item: {}", userId, itemId);
            CartResponse response = cartService.removeItemFromCart(userId, itemId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing item from cart: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            log.info("Clear cart - User: {}", userId);
            cartService.clearCart(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}