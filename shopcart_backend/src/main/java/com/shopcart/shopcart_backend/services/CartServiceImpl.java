package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.CartItemResponseDTO;
import com.shopcart.shopcart_backend.entities.*;
import com.shopcart.shopcart_backend.exception.BadRequestException;
import com.shopcart.shopcart_backend.exception.ResourceNotFoundException;
import com.shopcart.shopcart_backend.repositories.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartItemRepository cartItemRepository;

    @Override
    public CartItemResponseDTO addToCart(String email, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getStock() < quantity) {
            throw new BadRequestException("Not enough stock for product: " + product.getName());
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build();

        return CartItemResponseDTO.from(cartItemRepository.save(cartItem));
    }

    @Override
    public List<CartItemResponseDTO> getCartItemsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return cartItemRepository.findByUser(user).stream()
                .map(CartItemResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromCart(String email, Long cartItemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This cart item doesn't belong to the logged-in user");
        }

        cartItemRepository.delete(item);
    }

    @Override
    public double getTotalCartValue(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return cartItemRepository.findByUser(user).stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
}
