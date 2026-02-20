package com.karim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karim.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdAndDeletedFalse(Long userId);

	Optional<Order> findByIdAndDeletedFalse(Long id);

	List<Order> findByDeletedFalse();
}
