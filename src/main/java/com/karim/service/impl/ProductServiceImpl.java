package com.karim.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@Override
	public ProductResponse addProduct(ProductRequest dto) {
		Product pro = new Product();
		BeanUtils.copyProperties(dto, pro);
		Product newPro = repo.save(pro);

		ProductResponse res = new ProductResponse();
		BeanUtils.copyProperties(newPro, res);
		return res;
	}

	@Override
	public List<ProductResponse> getAllProducts() {
		List<Product> pros = repo.findByDeletedFalse();
		return pros.stream().map(pro -> {
			ProductResponse res = new ProductResponse();
			BeanUtils.copyProperties(pro, res);
			return res;
		}).collect(Collectors.toList());
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
	public ProductResponse updateProduct(Long id, ProductRequest dto) {

		Product product = repo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ProductNotFoundException("Product Not found with given Id : " + id));
		int oldStock = product.getStock(); // ✅ Save old stock
		BeanUtils.copyProperties(dto, product, "stock"); // ✅ Ignore stock
		product.setStock(oldStock + dto.getStock()); // ✅ Add properly
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
