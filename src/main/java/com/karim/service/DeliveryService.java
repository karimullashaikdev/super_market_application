package com.karim.service;

public interface DeliveryService {
	 
    void acceptOrder(Long orderId);
 
    void startDelivery(Long orderId);
 
    void completeDelivery(Long orderId, String enteredOtp);
}
 
