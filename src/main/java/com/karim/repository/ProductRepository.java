package com.karim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.karim.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	// ── existing methods (keep as-is) ──
	Page<Product> findByDeletedFalse(Pageable pageable);

	Optional<Product> findByIdAndDeletedFalse(Long id);

	List<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

	List<Product> findByCategoryAndDeletedFalse(String category);

	// ── new methods for paginated filtering ──
	Page<Product> findByCategoryIgnoreCaseAndDeletedFalse(String category, Pageable pageable);

	Page<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

	Page<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCaseAndDeletedFalse(String category, String name,
			Pageable pageable);

	// ── low-stock report ──
	List<Product> findByStockLessThanEqualAndDeletedFalse(int threshold);

	// ── fetch multiple products by id list (used in cart) ──
	List<Product> findByIdInAndDeletedFalse(List<Long> ids);
}
