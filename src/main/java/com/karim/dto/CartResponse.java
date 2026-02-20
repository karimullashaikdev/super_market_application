package com.karim.dto;

import lombok.Data;

@Data
public class CartResponse {

	private Long cartItemId;

	private Long productId;

	private String productName;

	private double price;

	private int quantity;

	private double totalPrice;
}
