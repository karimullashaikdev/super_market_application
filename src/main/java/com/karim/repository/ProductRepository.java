package com.karim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import com.karim.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByDeletedFalse();

	Optional<Product> findByIdAndDeletedFalse(Long id);

	List<Product> findByCategoryAndDeletedFalse(String category);

	List<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

	List<Product> findByIdInAndDeletedFalse(List<Long> ids);
	
	List<Product> findByStockLessThanEqualAndDeletedFalse(int threshold);

	Page<Product> findByDeletedFalse(Pageable pageable);

}
