package com.karim.dto;

import com.karim.enums.PaymentType;

import lombok.Data;

@Data
public class PaymentRequest {
	private Long orderId;
	private PaymentType paymentType;
}