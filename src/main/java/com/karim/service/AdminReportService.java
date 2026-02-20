package com.karim.service;

import java.util.List;

import com.karim.dto.AdminOrderReport;
import com.karim.dto.ItemSalesResponse;
import com.karim.dto.ProductSalesReport;

public interface AdminReportService {

	// Get all orders
	List<AdminOrderReport> getAllOrders();

	// Get total revenue
	Double getTotalRevenue();

	// Get low-stock products (threshold)
	List<ProductSalesReport> getLowStockProducts(int threshold);
	
	List<ItemSalesResponse> getItemWiseSales();

}
