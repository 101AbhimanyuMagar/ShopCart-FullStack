package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.CartItemResponseDTO;
import com.shopcart.shopcart_backend.services.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /*
     * Add item to cart
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartItemResponseDTO> addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            Authentication authentication) {

        String email = authentication.getName();

        log.info("🛒 Adding product {} to cart for user {}",
                productId,
                email);

        CartItemResponseDTO response =
                cartService.addToCart(email, productId, quantity);

        return ResponseEntity.ok(response);
    }

    /*
     * Get cart items
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CartItemResponseDTO>> getUserCart(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.getCartItemsByUser(email)
        );
    }

    /*
     * Remove cart item
     */
    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartItemId,
            Authentication authentication) {

        String email = authentication.getName();

        cartService.removeFromCart(email, cartItemId);

        log.info("❌ Removed cart item {} for user {}",
                cartItemId,
                email);

        return ResponseEntity.noContent().build();
    }

    /*
     * Total cart value
     */
    @GetMapping("/total-value")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Double> getTotalCartValue(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.getTotalCartValue(email)
        );
    }
}