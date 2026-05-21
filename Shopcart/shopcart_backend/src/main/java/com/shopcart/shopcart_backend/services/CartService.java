package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.CartItemResponseDTO;

import java.util.List;

public interface CartService {
    CartItemResponseDTO addToCart(String email, Long productId, int quantity);
    List<CartItemResponseDTO> getCartItemsByUser(String email);
    void removeFromCart(String email, Long cartItemId);
    double getTotalCartValue(String email);
}

