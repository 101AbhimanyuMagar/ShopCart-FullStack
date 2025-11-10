package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.*;
import com.shopcart.shopcart_backend.security.JwtUtil;
import com.shopcart.shopcart_backend.services.UserService;
import com.shopcart.shopcart_backend.security.CustomUserDetails;
import com.shopcart.shopcart_backend.security.CustomUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO userRequest) {
        UserResponseDTO response = userService.registerUser(userRequest);
        return ResponseEntity.status(201).body(response); // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String role = userDetails.getRole(); // ✅ Assuming you have this getter in CustomUserDetails

        return ResponseEntity.ok(new AuthResponse(token, role));
    }

}
