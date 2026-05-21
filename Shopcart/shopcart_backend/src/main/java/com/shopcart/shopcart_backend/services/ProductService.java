package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import com.shopcart.shopcart_backend.entities.Discount;
import com.shopcart.shopcart_backend.entities.Product;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.io.IOException;
import java.util.Date;

public interface ProductService {
    ProductResponseDTO addProduct(ProductRequestDTO productRequest, MultipartFile imageFile) throws IOException;

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequest, MultipartFile imageFile)
            throws IOException; // updated signature

    void deleteProduct(Long id);

    // ✅ Discount-related methods (must match implementation signatures)
    Discount addDiscount(Long productId, double percentage, Date startDate, Date endDate);

    Discount updateDiscount(Long productId, double percentage, Date startDate, Date endDate);

    void removeDiscount(Long productId);

    double getEffectivePrice(Product product);

     List<ProductResponseDTO> getProducts(Long categoryId, String search, String sortBy, int page, int size);
}
