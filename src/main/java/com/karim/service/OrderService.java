package com.karim.service;

import java.util.List;

import com.karim.dto.CheckoutRequest;
import com.karim.entity.Order;

public interface OrderService {

	Order checkout(Long userId, CheckoutRequest request);

	List<Order> getMyOrders(Long userId);

	Order getOrder(Long orderId, Long userId);
}
