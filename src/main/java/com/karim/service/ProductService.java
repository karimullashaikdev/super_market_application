package com.karim.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;

public interface ProductService {

	ProductResponse addProduct(ProductRequest dto);

	Page<ProductResponse> getAllProducts(int page, int size);

	ProductResponse getProductById(Long id);

	ProductResponse updateProduct(Long id, ProductRequest dto);

	void deleteProduct(Long id);

	List<ProductResponse> searchByName(String name);

	List<ProductResponse> getByCategory(String category);
}
