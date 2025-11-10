package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import com.shopcart.shopcart_backend.entities.Product;
import com.shopcart.shopcart_backend.entities.Role;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.exception.ResourceNotFoundException;
import com.shopcart.shopcart_backend.repositories.ProductRepository;
import com.shopcart.shopcart_backend.repositories.UserRepository;
import com.shopcart.shopcart_backend.security.CustomUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;


@Override
public ProductResponseDTO addProduct(ProductRequestDTO request, MultipartFile image) throws IOException {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getRole() != Role.ADMIN) {
        throw new AccessDeniedException("Only admin users can add products");
    }

    String imageUrl = null;
    if (image != null && !image.isEmpty()) {
        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
        Path uploadPath = Paths.get(uploadDir); // from application.properties
        Files.createDirectories(uploadPath);
        Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        imageUrl = "product-images/" + fileName;
    }

    Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .imageUrl(imageUrl)
            .addedBy(user)
            .build();

    Product saved = productRepository.save(product);
    return ProductResponseDTO.from(saved);
}





    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return ProductResponseDTO.from(product);
    }

   @Override
public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

    Long currentAdminId = getCurrentUserId();

    // Ensure only the admin who added it can delete
    if (!product.getAddedBy().getId().equals(currentAdminId)) {
        throw new AccessDeniedException("You are not authorized to delete this product.");
    }

    productRepository.delete(product);
}

@Override
public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request, MultipartFile imageFile) throws IOException {
    Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

    Long currentAdminId = getCurrentUserId();
    if (!existing.getAddedBy().getId().equals(currentAdminId)) {
        throw new AccessDeniedException("You are not authorized to update this product.");
    }

    existing.setName(request.getName());
    existing.setDescription(request.getDescription());
    existing.setPrice(request.getPrice());
    existing.setStock(request.getStock());

    if (imageFile != null && !imageFile.isEmpty()) {
        // Delete old image
        if (existing.getImageUrl() != null) {
            Path oldImagePath = Paths.get(uploadDir, existing.getImageUrl().replace("product-images/", ""));
            Files.deleteIfExists(oldImagePath);
        }

        // Save new image
        String originalFilename = imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename() : "unknown.jpg";
        String cleanName = StringUtils.cleanPath(originalFilename);
        String fileName = UUID.randomUUID() + "_" + cleanName;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        // Store relative path in DB
        existing.setImageUrl("product-images/" + fileName);
    }

    Product updated = productRepository.save(existing);
    return ProductResponseDTO.from(updated);
}

private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new AccessDeniedException("Unauthorized access");
    }

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    return userDetails.getUser().getId();

}

}

