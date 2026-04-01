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

        String imageUrl = null;

        // ✅ Handle image safely
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(image);
        }

        Product pro = new Product();
        BeanUtils.copyProperties(dto, pro);

        // ✅ Only set image if available
        if (imageUrl != null) {
            pro.setImageUrl(imageUrl);
        }

        Product saved = repo.save(pro);

        ProductResponse res = new ProductResponse();
        BeanUtils.copyProperties(saved, res);

        return res;
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size, String category, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        boolean hasCategory = category != null && !category.isBlank();
        boolean hasSearch   = search   != null && !search.isBlank();

        Page<Product> result;

        if (hasCategory && hasSearch) {
            // Both filters: category match + name contains search keyword
            result = repo.findByCategoryIgnoreCaseAndNameContainingIgnoreCaseAndDeletedFalse(
                    category, search, pageable);
        } else if (hasCategory) {
            // Category filter only
            result = repo.findByCategoryIgnoreCaseAndDeletedFalse(category, pageable);
        } else if (hasSearch) {
            // Search keyword only
            result = repo.findByNameContainingIgnoreCaseAndDeletedFalse(search, pageable);
        } else {
            // No filters — return all products
            result = repo.findByDeletedFalse(pageable);
        }

        return result.map(pro -> {
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
        BeanUtils.copyProperties(dto, product, "stock");
        product.setStock(dto.getStock());
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
        return repo.findByNameContainingIgnoreCaseAndDeletedFalse(name).stream().map(p -> {
            ProductResponse res = new ProductResponse();
            BeanUtils.copyProperties(p, res);
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getByCategory(String category) {
        return repo.findByCategoryAndDeletedFalse(category).stream().map(p -> {
            ProductResponse res = new ProductResponse();
            BeanUtils.copyProperties(p, res);
            return res;
        }).collect(Collectors.toList());
    }
}
