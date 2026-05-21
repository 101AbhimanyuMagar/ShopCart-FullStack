package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import com.shopcart.shopcart_backend.entities.Discount;
import com.shopcart.shopcart_backend.entities.Product;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.repositories.ProductRepository;
import com.shopcart.shopcart_backend.repositories.UserRepository;
import com.shopcart.shopcart_backend.services.ProductService;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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

    // ✅ Add product
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponseDTO> addProduct(
            @RequestPart("product") ProductRequestDTO productDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile)
            throws IOException {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.info("🛒 Admin {} adding product: {}",
                email,
                productDTO.getName());

        ProductResponseDTO response =
                productService.addProduct(productDTO, imageFile);

        log.info("✅ Product added successfully");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ✅ Get all products
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {

        log.info("📦 Fetching all products");

        List<ProductResponseDTO> products = productRepository.findAll()
                .stream()
                .map(ProductResponseDTO::from)
                .collect(Collectors.toList());

        log.info("✅ Total products fetched: {}", products.size());

        return ResponseEntity.ok(products);
    }

    // ✅ Get products added by admin
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/my-products")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByLoggedInAdmin() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.info("📦 Fetching products for admin: {}", email);

        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<ProductResponseDTO> products = productRepository.findByAddedBy(admin)
                .stream()
                .map(ProductResponseDTO::from)
                .collect(Collectors.toList());

        log.info("✅ Admin product count: {}", products.size());

        return ResponseEntity.ok(products);
    }

    // ✅ Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable Long id) {

        log.info("🔍 Fetching product with ID: {}", id);

        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    // ✅ Update product
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") ProductRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile)
            throws IOException {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.info("✏️ Admin {} updating product ID: {}",
                email,
                id);

        ProductResponseDTO response =
                productService.updateProduct(id, request, imageFile);

        log.info("✅ Product updated successfully");

        return ResponseEntity.ok(response);
    }

    // ✅ Delete product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.info("🗑️ Admin {} deleting product ID: {}",
                email,
                id);

        productService.deleteProduct(id);

        log.info("✅ Product deleted successfully");

        return ResponseEntity.noContent().build();
    }

    // ✅ Add discount
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}/discount")
    public ResponseEntity<Discount> addDiscount(
            @PathVariable Long productId,
            @RequestParam double percentage,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date endDate) {

        log.info("🏷️ Adding discount for product ID: {}", productId);

        return ResponseEntity.ok(
                productService.addDiscount(
                        productId,
                        percentage,
                        startDate,
                        endDate
                )
        );
    }

    // ✅ Update discount
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/discount")
    public ResponseEntity<Discount> updateDiscount(
            @PathVariable Long productId,
            @RequestParam double percentage,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date endDate) {

        log.info("✏️ Updating discount for product ID: {}", productId);

        return ResponseEntity.ok(
                productService.updateDiscount(
                        productId,
                        percentage,
                        startDate,
                        endDate
                )
        );
    }

    // ✅ Remove discount
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}/discount")
    public ResponseEntity<Void> removeDiscount(
            @PathVariable Long productId) {

        log.info("❌ Removing discount from product ID: {}", productId);

        productService.removeDiscount(productId);

        return ResponseEntity.noContent().build();
    }

    // ✅ Filter / sort / pagination
    @GetMapping("/filter")
    public List<ProductResponseDTO> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        log.info("📦 Filtering products | category={} | search={} | sortBy={} | page={}",
                categoryId,
                search,
                sortBy,
                page);

        return productService.getProducts(
                categoryId,
                search,
                sortBy,
                page,
                size
        );
    }
}