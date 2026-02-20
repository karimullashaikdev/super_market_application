package com.karim.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karim.dto.AdminOrderReport;
import com.karim.dto.ItemSalesResponse;
import com.karim.dto.ProductSalesReport;
import com.karim.entity.Order;
import com.karim.entity.OrderItem;
import com.karim.enums.OrderStatus;
import com.karim.repository.OrderRepository;
import com.karim.repository.ProductRepository;
import com.karim.service.AdminReportService;

@Service
public class AdminReportServiceImpl implements AdminReportService {

	@Autowired
	private OrderRepository orderRepo;

	@Autowired
	private ProductRepository productRepo;

	// =================================
	// Get all orders
	// =================================
	@Override
	public List<AdminOrderReport> getAllOrders() {

		return orderRepo.findByDeletedFalse().stream().map(o -> {
			AdminOrderReport r = new AdminOrderReport();
			r.setOrderId(o.getId());
			r.setUserId(o.getUserId());
			r.setStatus(o.getStatus() != null ? o.getStatus().name() : null);
			r.setTotalAmount(o.getTotalAmount());
			r.setCreatedAt(o.getCreatedAt());
			return r;
		}).toList();
	}

	// =================================
	// Get total revenue
	// =================================
	@Override
	public Double getTotalRevenue() {

		return orderRepo.findByDeletedFalse().stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
				.mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0).sum();
	}

	// =================================
	// Low-stock products
	// =================================
	@Override
	public List<ProductSalesReport> getLowStockProducts(int threshold) {

		return productRepo.findByStockLessThanEqualAndDeletedFalse(threshold).stream().map(p -> {
			ProductSalesReport r = new ProductSalesReport();
			r.setProductId(p.getId());
			r.setProductName(p.getName());
			r.setStock(p.getStock());
			return r;
		}).toList();
	}

	// =================================
	// Item-wise sales report
	// =================================
	@Override
	public List<ItemSalesResponse> getItemWiseSales() {

		List<Order> orders = orderRepo.findByDeletedFalse();

		Map<Long, ItemSalesResponse> salesMap = new HashMap<>();

		for (Order order : orders) {

			// Only count successful orders
			if (order.getStatus() == null || order.getStatus() != OrderStatus.CONFIRMED) {
				continue;
			}

			if (order.getItems() == null)
				continue;

			for (OrderItem item : order.getItems()) {

				ItemSalesResponse r = salesMap.getOrDefault(item.getProductId(), new ItemSalesResponse());

				r.setProductId(item.getProductId());
				r.setProductName(item.getProductName());

				long updatedQty = (r.getQuantitySold() == null ? 0 : r.getQuantitySold()) + item.getQuantity();

				double updatedRevenue = (r.getRevenue() == null ? 0.0 : r.getRevenue())
						+ (item.getPrice() * item.getQuantity());

				r.setQuantitySold(updatedQty);
				r.setRevenue(updatedRevenue);

				salesMap.put(item.getProductId(), r);
			}
		}

		return salesMap.values().stream().sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue())) // Sort by revenue
																										// desc
				.toList();
	}

}
