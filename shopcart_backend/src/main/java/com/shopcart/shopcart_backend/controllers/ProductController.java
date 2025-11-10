package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.repositories.ProductRepository;
import com.shopcart.shopcart_backend.repositories.UserRepository;
import com.shopcart.shopcart_backend.services.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ProductController(ProductService productService,
                             ProductRepository productRepository,
                             UserRepository userRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ✅ Add a product (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponseDTO> addProduct(
            @RequestPart("product") ProductRequestDTO productDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        return new ResponseEntity<>(productService.addProduct(productDTO, imageFile), HttpStatus.CREATED);
    }

    // ✅ Get all products (Accessible to everyone)
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ✅ Get products added by the logged-in admin
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/my-products")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByLoggedInAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<ProductResponseDTO> products = productRepository.findByAddedBy(admin)
                .stream()
                .map(ProductResponseDTO::from) // ✅ use your static mapper
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ✅ Update product (Admin only)
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") ProductRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, request, imageFile));
    }

    // ✅ Delete product (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
