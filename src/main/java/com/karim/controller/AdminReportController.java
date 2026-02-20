package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.AdminOrderReport;
import com.karim.dto.ItemSalesResponse;
import com.karim.dto.ProductSalesReport;
import com.karim.service.AdminReportService;

@RestController
@RequestMapping("/api/admin")
public class AdminReportController {

	@Autowired
	private AdminReportService reportService;

	// =================================
	// Get all orders
	// =================================
	@GetMapping("/orders")
	public ResponseEntity<List<AdminOrderReport>> getAllOrders() {
		return ResponseEntity.ok(reportService.getAllOrders());
	}

	// =================================
	// Get total revenue
	// =================================
	@GetMapping("/revenue")
	public ResponseEntity<Double> getTotalRevenue() {
		return ResponseEntity.ok(reportService.getTotalRevenue());
	}

	// =================================
	// Low-stock products
	// =================================
	@GetMapping("/low-stock/{threshold}")
	public ResponseEntity<List<ProductSalesReport>> getLowStock(@PathVariable int threshold) {
		return ResponseEntity.ok(reportService.getLowStockProducts(threshold));
	}

	// =================================
	// Get Item wise Sales
	// =================================
	@GetMapping("/item-sales")
	public ResponseEntity<List<ItemSalesResponse>> getItemWiseSales() {
		return ResponseEntity.ok(reportService.getItemWiseSales());
	}

}
