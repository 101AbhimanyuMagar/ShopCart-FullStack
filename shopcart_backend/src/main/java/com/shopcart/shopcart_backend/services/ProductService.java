package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.ProductRequestDTO;
import com.shopcart.shopcart_backend.dto.ProductResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.io.IOException;

public interface ProductService {
    ProductResponseDTO addProduct(ProductRequestDTO productRequest, MultipartFile imageFile) throws IOException;
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO getProductById(Long id);
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequest, MultipartFile imageFile) throws IOException; // updated signature
    void deleteProduct(Long id);
}
