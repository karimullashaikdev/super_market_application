package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;
import com.karim.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	@Autowired
	private ProductService service;

//	@PostMapping
//	public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest req) {
//		ProductResponse product = service.addProduct(req);
//		return ResponseEntity.status(HttpStatus.CREATED).body(product);
//	}
	@PostMapping(consumes = "multipart/form-data")
	public ResponseEntity<ProductResponse> addProduct(@RequestPart("product") ProductRequest req,
			@RequestPart(value = "image", required = true) MultipartFile image) { // required = true → mandatory
		try {
			ProductResponse product = service.addProduct(req, image);
			return ResponseEntity.status(HttpStatus.CREATED).body(product);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(null);
		}
	}

//	@GetMapping
//	public ResponseEntity<List<ProductResponse>> getAllProducts() {
//		List<ProductResponse> allProducts = service.getAllProducts();
//		return ResponseEntity.status(HttpStatus.OK).body(allProducts);
//	}

	@GetMapping
	public ResponseEntity<Page<ProductResponse>> getAllProducts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllProducts(page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		ProductResponse product = service.getProductById(id);
		return ResponseEntity.status(HttpStatus.OK).body(product);
	}

	@PutMapping(value = "/{id}", consumes = "multipart/form-data")
	public ResponseEntity<ProductResponse> updateProductById(@PathVariable Long id,
			@RequestPart("product") ProductRequest request,
			@RequestPart(value = "image", required = false) MultipartFile image) { // required = false → image optional
																					// on update
		try {
			ProductResponse updated = service.updateProduct(id, request, image);
			return ResponseEntity.status(HttpStatus.OK).body(updated);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(null);
		}
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
