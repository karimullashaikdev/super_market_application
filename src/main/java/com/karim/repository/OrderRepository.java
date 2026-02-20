package com.karim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.karim.dto.OrderItemDetailsDTO;
import com.karim.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdAndDeletedFalse(Long userId);

	Optional<Order> findByIdAndDeletedFalse(Long id);

	List<Order> findByDeletedFalse();

	@Query("""
		    SELECT new com.karim.dto.OrderItemDetailsDTO(
		        o.id,
		        o.userId,
		        o.paymentType,
		        o.status,
		        o.totalAmount,
		        o.createdAt,
		        oi.id,
		        oi.productId,
		        oi.productName,
		        oi.quantity,
		        oi.price
		    )
		    FROM Order o
		    JOIN o.items oi
		""")
		List<OrderItemDetailsDTO> fetchOrderItemDetails();
}
