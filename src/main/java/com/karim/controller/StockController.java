package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.ProductSalesReport;
import com.karim.dto.StockUpdateRequest;
import com.karim.service.StockService;

@RestController
@RequestMapping("/api/admin/stock")
public class StockController {

	@Autowired
	private StockService stockService;

	// =================================
	// Admin: Update stock
	// =================================
	@PostMapping("/update")
	public ResponseEntity<String> updateStock(@RequestBody StockUpdateRequest request) {
		stockService.updateStock(request);
		return ResponseEntity.ok("Stock updated successfully");
	}
}
