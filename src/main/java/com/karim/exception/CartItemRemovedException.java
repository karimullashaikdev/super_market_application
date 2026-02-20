package com.karim.exception;

public class CartItemRemovedException extends RuntimeException {
	public CartItemRemovedException(String msg) {
		super(msg);
	}
}
