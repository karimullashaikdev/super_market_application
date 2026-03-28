package com.karim.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;

public interface ProductService {

    ProductResponse addProduct(ProductRequest dto, MultipartFile image) throws IOException;

    // Updated: accepts optional category and search filters
    Page<ProductResponse> getAllProducts(int page, int size, String category, String search);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest dto, MultipartFile image) throws IOException;

    void deleteProduct(Long id);

    List<ProductResponse> searchByName(String name);

    List<ProductResponse> getByCategory(String category);
}
