package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.CartItemResponseDTO;
import com.shopcart.shopcart_backend.entities.Role;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.repositories.UserRepository;
import com.shopcart.shopcart_backend.services.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    // ✅ Add item to cart
    @PostMapping("/add")
    public ResponseEntity<CartItemResponseDTO> addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null); // Admin/Super Admin cannot use cart
        }

        CartItemResponseDTO response = cartService.addToCart(currentUser.getEmail(), productId, quantity);
        return ResponseEntity.ok(response);
    }

    // ✅ Get all cart items for current user
    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getUserCart(Authentication authentication) {
        String email = authentication.getName();
        List<CartItemResponseDTO> items = cartService.getCartItemsByUser(email);
        return ResponseEntity.ok(items);
    }

    // ✅ Remove item from cart
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId, Authentication authentication) {
        String email = authentication.getName();
        cartService.removeFromCart(email, cartItemId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ✅ Get total cart value
    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalCartValue(Authentication authentication) {
        String email = authentication.getName();
        double total = cartService.getTotalCartValue(email);
        return ResponseEntity.ok(total);
    }
}
