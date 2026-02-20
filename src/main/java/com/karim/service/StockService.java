package com.karim.service;

import com.karim.dto.StockUpdateRequest;
import com.karim.dto.ProductSalesReport;
import java.util.List;

public interface StockService {

    // Reduce stock after successful order
    void reduceStockAfterOrder(Long orderId);

    // Admin: Update stock manually
    void updateStock(StockUpdateRequest request);
}
