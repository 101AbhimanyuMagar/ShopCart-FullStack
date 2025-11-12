package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import com.shopcart.shopcart_backend.entities.Discount;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

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
    List<ProductResponseDTO> products = productRepository.findAll()
            .stream()
            .map(ProductResponseDTO::from) // includes discounted price & discount info
            .collect(Collectors.toList());

    return ResponseEntity.ok(products);
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


    // ===============================
// ✅ DISCOUNT MANAGEMENT (ADMIN)
// ===============================

// ✅ Add discount
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{productId}/discount")
public ResponseEntity<Discount> addDiscount(
        @PathVariable Long productId,
        @RequestParam double percentage,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
    return ResponseEntity.ok(productService.addDiscount(productId, percentage, startDate, endDate));
}

// ✅ Update discount
@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{productId}/discount")
public ResponseEntity<Discount> updateDiscount(
        @PathVariable Long productId,
        @RequestParam double percentage,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
    return ResponseEntity.ok(productService.updateDiscount(productId, percentage, startDate, endDate));
}

// ✅ Remove discount
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{productId}/discount")
public ResponseEntity<Void> removeDiscount(@PathVariable Long productId) {
    productService.removeDiscount(productId);
    return ResponseEntity.noContent().build();
}

}
