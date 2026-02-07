package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CartItemRequest;
import com.ecommerce.orderservice.dto.CartItemResponse;
import com.ecommerce.orderservice.dto.CartResponse;
import com.ecommerce.orderservice.entity.Cart;
import com.ecommerce.orderservice.entity.CartItem;
import com.ecommerce.orderservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClientService productClientService;

    @Transactional
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        log.info("Adding item to cart - User: {}, Product: {}", userId, request.getProductId());

        // Get product details from Product Service
        var productResponse = productClientService.getProduct(request.getProductId());

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .productId(productResponse.getId())
                    .productName(productResponse.getName())
                    .price(productResponse.getPrice())
                    .quantity(request.getQuantity())
                    .build();
            cart.addCartItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart successfully");

        return mapToCartResponse(savedCart);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, Integer quantity) {
        log.info("Updating cart item - User: {}, Item: {}, Quantity: {}", userId, itemId, quantity);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cart.getCartItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));

        if (quantity <= 0) {
            cart.removeCartItem(item);
        } else {
            item.setQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart item updated successfully");

        return mapToCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeItemFromCart(Long userId, Long itemId) {
        log.info("Removing item from cart - User: {}, Item: {}", userId, itemId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cart.getCartItems().removeIf(item -> item.getId().equals(itemId));

        Cart savedCart = cartRepository.save(cart);
        log.info("Item removed from cart successfully");

        return mapToCartResponse(savedCart);
    }

    public CartResponse getCart(Long userId) {
        log.info("Fetching cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });

        return mapToCartResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cart.clearItems();
        cartRepository.save(cart);

        log.info("Cart cleared successfully");
    }

    private CartResponse mapToCartResponse(Cart cart) {
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getCartItems().stream()
                        .map(this::mapToCartItemResponse)
                        .collect(Collectors.toList()))
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}