package com.karim.dto;

import com.karim.enums.PaymentType;

import lombok.Data;

@Data
public class CheckoutRequest {

	private PaymentType paymentType;
	
	 // ✅ ADD THIS
    private String address;
}
