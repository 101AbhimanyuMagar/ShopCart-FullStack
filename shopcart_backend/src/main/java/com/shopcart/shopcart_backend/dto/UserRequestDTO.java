package com.shopcart.shopcart_backend.dto;

import com.shopcart.shopcart_backend.entities.Role;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String name;
    private String email;
    private String password; // Only for registration
     private Role role;
}