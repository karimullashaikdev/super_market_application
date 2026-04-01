package com.karim.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;
import com.karim.service.ProductService;


@RestController
@RequestMapping("/api/products")
public class ProductController {

	@Autowired
	private ProductService service;

//	@PostMapping(consumes = "multipart/form-data")
//	public ResponseEntity<ProductResponse> addProduct(@RequestPart("product") ProductRequest req,
//			@RequestPart(value = "image", required = false) MultipartFile image) {
//
//		try {
//			ProductResponse product = service.addProduct(req, image);
//			return ResponseEntity.status(HttpStatus.CREATED).body(product);
//		} catch (Exception e) {
//			return ResponseEntity.badRequest().body(null);
//		}
//	}
	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addProduct(
	        @RequestPart("product") String productJson,
	        @RequestPart(value = "image", required = false) MultipartFile image) {

	    try {
	        // 🔍 Log incoming JSON (for debugging)
	        System.out.println("Incoming product JSON: " + productJson);

	        // ✅ Convert JSON -> Object
	        ObjectMapper mapper = new ObjectMapper();
	        ProductRequest req = mapper.readValue(productJson, ProductRequest.class);

	        // ✅ Validate image (important)
	        if (image != null && !image.isEmpty()) {
	            System.out.println("Image received: " + image.getOriginalFilename());
	        }

	        // ✅ Call service
	        ProductResponse product = service.addProduct(req, image);

	        return ResponseEntity.status(HttpStatus.CREATED).body(product);

	    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
	        // 🔥 JSON parsing issue
	        return ResponseEntity.badRequest().body(
	                Map.of(
	                        "error", "Invalid JSON format",
	                        "message", e.getOriginalMessage()
	                )
	        );

	    } catch (Exception e) {
	        // 🔥 General error
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                Map.of(
	                        "error", "Something went wrong",
	                        "message", e.getMessage()
	                )
	        );
	    }
	}

	// ── Main paginated endpoint now supports optional category + search filters ──
	@GetMapping
	public ResponseEntity<Page<ProductResponse>> getAllProducts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String category,
			@RequestParam(required = false) String search) {
		return ResponseEntity.ok(service.getAllProducts(page, size, category, search));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		ProductResponse product = service.getProductById(id);
		return ResponseEntity.status(HttpStatus.OK).body(product);
	}

	@PutMapping(value = "/{id}", consumes = "multipart/form-data")
	public ResponseEntity<ProductResponse> updateProductById(@PathVariable Long id,
			@RequestPart("product") ProductRequest request,
			@RequestPart(value = "image", required = false) MultipartFile image) {
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

	// Keep these legacy endpoints so nothing else breaks
	@GetMapping("/search")
	public List<ProductResponse> search(@RequestParam String name) {
		return service.searchByName(name);
	}

	@GetMapping("/category")
	public List<ProductResponse> category(@RequestParam String cat) {
		return service.getByCategory(cat);
	}
}
