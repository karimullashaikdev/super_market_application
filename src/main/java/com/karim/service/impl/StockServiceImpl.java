package com.karim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karim.dto.StockUpdateRequest;
import com.karim.entity.Order;
import com.karim.entity.OrderItem;
import com.karim.entity.Product;
import com.karim.exception.ProductNotFoundException;
import com.karim.repository.OrderRepository;
import com.karim.repository.ProductRepository;
import com.karim.service.StockService;

@Service
public class StockServiceImpl implements StockService {

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private OrderRepository orderRepo;

	// =================================
	// Reduce stock after successful order
	// =================================
	@Override
	@Transactional
	public void reduceStockAfterOrder(Long orderId) {
		Order order = orderRepo.findByIdAndDeletedFalse(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found"));

		if (!"PAID".equals(order.getStatus())) {
			throw new RuntimeException("Cannot reduce stock for unpaid order");
		}

		for (OrderItem item : order.getItems()) {
			Product product = productRepo.findByIdAndDeletedFalse(item.getProductId())
					.orElseThrow(() -> new ProductNotFoundException("Product not found: " + item.getProductId()));

			int newStock = product.getStock() - item.getQuantity();
			if (newStock < 0)
				newStock = 0;

			product.setStock(newStock);
			productRepo.save(product);
		}
	}

	// =================================
	// Admin: update stock manually
	// =================================
	@Override
	@Transactional
	public void updateStock(StockUpdateRequest request) {
		Product product = productRepo.findByIdAndDeletedFalse(request.getProductId())
				.orElseThrow(() -> new ProductNotFoundException("Product not found"));

		if (request.isSetExact()) {
			product.setStock(request.getQuantity());
		} else {
			product.setStock(product.getStock() + request.getQuantity());
		}

		productRepo.save(product);
	}
}
