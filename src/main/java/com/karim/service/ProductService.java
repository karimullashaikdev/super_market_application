package com.karim.service;

import java.util.List;
import java.util.Optional;

import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;
import com.karim.entity.Product;

public interface ProductService {

	ProductResponse addProduct(ProductRequest dto);

	List<ProductResponse> getAllProducts();

	ProductResponse getProductById(Long id);

	ProductResponse updateProduct(Long id, ProductRequest dto);

	void deleteProduct(Long id);

	List<ProductResponse> searchByName(String name);

	List<ProductResponse> getByCategory(String category);
}
