package com.karim.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.karim.dto.ProductRequest;
import com.karim.dto.ProductResponse;
import com.karim.entity.Product;
import com.karim.exception.ProductNotFoundException;
import com.karim.repository.ProductRepository;
import com.karim.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository repo;

	@Autowired
	private CloudinaryService cloudinaryService;

	@Override
	public ProductResponse addProduct(ProductRequest dto, MultipartFile image) throws IOException {

		// Step 1 — upload image to Cloudinary, get URL
		String imageUrl = cloudinaryService.uploadImage(image);

		// Step 2 — create product and set all fields
		Product pro = new Product();
		BeanUtils.copyProperties(dto, pro);
		pro.setImageUrl(imageUrl); // ✅ set the URL we got from Cloudinary

		// Step 3 — save in DB
		Product saved = repo.save(pro);

		// Step 4 — return response
		ProductResponse res = new ProductResponse();
		BeanUtils.copyProperties(saved, res);
		return res;
	}

//	@Override
//	public List<ProductResponse> getAllProducts() {
//		List<Product> pros = repo.findByDeletedFalse();
//		return pros.stream().map(pro -> {
//			ProductResponse res = new ProductResponse();
//			BeanUtils.copyProperties(pro, res);
//			return res;
//		}).collect(Collectors.toList());
//	}

	@Override
	public Page<ProductResponse> getAllProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		return repo.findByDeletedFalse(pageable).map(pro -> {
			ProductResponse res = new ProductResponse();
			BeanUtils.copyProperties(pro, res);
			return res;
		});
	}

	@Override
	public ProductResponse getProductById(Long id) {
		Product product = repo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ProductNotFoundException("Product Not found with given Id :" + id));
		ProductResponse res = new ProductResponse();
		BeanUtils.copyProperties(product, res);
		return res;
	}

	@Override
	public ProductResponse updateProduct(Long id, ProductRequest dto, MultipartFile image) throws IOException {

		Product product = repo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ProductNotFoundException("Product Not found with given Id : " + id));

		// copy name, category, price, brand
		BeanUtils.copyProperties(dto, product, "stock");
		product.setStock(dto.getStock());

		// ✅ if admin sends a new image → upload to Cloudinary and update URL
		// ✅ if no image sent → keep the existing image as it is
		if (image != null && !image.isEmpty()) {
			String newImageUrl = cloudinaryService.uploadImage(image);
			product.setImageUrl(newImageUrl);
		}

		Product updated = repo.save(product);
		ProductResponse res = new ProductResponse();
		BeanUtils.copyProperties(updated, res);
		return res;
	}

	@Override
	public void deleteProduct(Long id) {
		Product product = repo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ProductNotFoundException("Product Not found with given Id : " + id));
		product.setDeleted(true);
		repo.save(product);
	}

	@Override
	public List<ProductResponse> searchByName(String name) {

		List<Product> products = repo.findByNameContainingIgnoreCaseAndDeletedFalse(name);

		return products.stream().map(p -> {

			ProductResponse res = new ProductResponse();

			BeanUtils.copyProperties(p, res);

			return res;
		}).collect(Collectors.toList());
	}

	@Override
	public List<ProductResponse> getByCategory(String category) {

		List<Product> products = repo.findByCategoryAndDeletedFalse(category);

		return products.stream().map(p -> {

			ProductResponse res = new ProductResponse();

			BeanUtils.copyProperties(p, res);

			return res;
		}).collect(Collectors.toList());
	}

}
