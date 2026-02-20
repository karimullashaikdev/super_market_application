package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;
import com.karim.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	@Autowired
	private ProductService service;

	@PostMapping
	public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest req) {
		ProductResponse product = service.addProduct(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(product);
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getAllProducts() {
		List<ProductResponse> allProducts = service.getAllProducts();
		return ResponseEntity.status(HttpStatus.OK).body(allProducts);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		ProductResponse product = service.getProductById(id);
		return ResponseEntity.status(HttpStatus.OK).body(product);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProductById(@PathVariable Long id,
			@RequestBody ProductRequest request) {
		ProductResponse updateProduct = service.updateProduct(id, request);
		return ResponseEntity.status(HttpStatus.OK).body(updateProduct);
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {

		service.deleteProduct(id);
		return "Product Deleted";
	}

	// Search
	@GetMapping("/search")
	public List<ProductResponse> search(@RequestParam String name) {

		return service.searchByName(name);
	}

	// Category
	@GetMapping("/category")
	public List<ProductResponse> category(@RequestParam String cat) {

		return service.getByCategory(cat);
	}
}
