package com.shopcart.shopcart_backend.services;

import java.util.List;

import com.shopcart.shopcart_backend.dto.UserRequestDTO;
import com.shopcart.shopcart_backend.dto.UserResponseDTO;


public interface UserService {
    UserResponseDTO registerUser(UserRequestDTO userRequest); // For incoming registration request
    UserResponseDTO getUserByEmail(String email);             // For returning sanitized user info
    List<UserResponseDTO> getAllUsers();                      // For admin
}

