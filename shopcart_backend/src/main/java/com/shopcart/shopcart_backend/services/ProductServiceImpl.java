package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import com.shopcart.shopcart_backend.entities.Discount;
import com.shopcart.shopcart_backend.entities.Product;
import com.shopcart.shopcart_backend.entities.Role;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.exception.ResourceNotFoundException;
import com.shopcart.shopcart_backend.repositories.DiscountRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscountRepository discountRepository;

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
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request, MultipartFile imageFile)
            throws IOException {
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
            String originalFilename = imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename()
                    : "unknown.jpg";
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

    // ✅ Add discount to a product
    @Override
    @Transactional
    public Discount addDiscount(Long productId, double percentage, Date startDate, Date endDate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Discount> existing = discountRepository.findActiveDiscountForProduct(productId, new Date());
        if (existing.isPresent()) {
            throw new RuntimeException("Discount already exists for this product.");
        }

        Discount discount = Discount.builder()
                .product(product)
                .percentage(percentage)
                .startDate(startDate != null ? startDate : new Date())
                .endDate(endDate)
                .active(true)
                .build();

        return discountRepository.save(discount);
    }

    // ✅ Update an existing discount
    @Override
    @Transactional
    public Discount updateDiscount(Long productId, double percentage, Date startDate, Date endDate) {
        Discount discount = discountRepository.findActiveDiscountForProduct(productId, new Date())
                .orElseThrow(() -> new RuntimeException("No active discount found for this product."));

        discount.setPercentage(percentage);
        if (startDate != null)
            discount.setStartDate(startDate);
        if (endDate != null)
            discount.setEndDate(endDate);

        return discountRepository.save(discount);
    }

@Override
@Transactional
public void removeDiscount(Long productId) {
    discountRepository.findActiveDiscountForProduct(productId, new Date())
            .ifPresent(discount -> {
                discount.setActive(false);  // mark as inactive
                discount.setEndDate(new Date()); // optional: set end date to now
                discountRepository.save(discount);
            });
}


    public double getEffectivePrice(Product product) {
    if (product == null) return 0.0;

    Date now = new Date();

    return product.getDiscounts().stream()
            .filter(Discount::isActive)
            .filter(d -> {
                Date start = d.getStartDate();
                Date end = d.getEndDate();
                boolean withinStart = (start == null) || !now.before(start);
                boolean withinEnd = (end == null) || !now.after(end);
                return withinStart && withinEnd;
            })
            .findFirst()
            .map(d -> {
                double price = product.getPrice();
                return price - (price * d.getPercentage() / 100);
            })
            .orElse(product.getPrice());
}


}
