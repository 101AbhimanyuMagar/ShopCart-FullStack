package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.*;
import com.shopcart.shopcart_backend.security.CustomUserDetails;
import com.shopcart.shopcart_backend.security.CustomUserDetailsService;
import com.shopcart.shopcart_backend.security.JwtUtil;
import com.shopcart.shopcart_backend.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    /*
     * Register User
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @RequestBody UserRequestDTO userRequest) {

        log.info("🟢 Register request received for email: {}",
                userRequest.getEmail());

        UserResponseDTO response =
                userService.registerUser(userRequest);

        log.info("✅ User registered successfully: {}",
                userRequest.getEmail());

        return ResponseEntity.status(201).body(response);
    }

    /*
     * Login User
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request) {

        log.info("🟡 Login attempt for email: {}",
                request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService
                        .loadUserByUsername(request.getEmail());

        String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getRole();

        log.info("✅ Login successful for user: {}",
                request.getEmail());

        return ResponseEntity.ok(
                new AuthResponse(token, role)
        );
    }
}