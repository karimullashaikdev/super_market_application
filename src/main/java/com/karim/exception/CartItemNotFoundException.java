package com.karim.exception;

public class CartItemNotFoundException extends RuntimeException{
	public CartItemNotFoundException(String msg) {
		super(msg);
	}
}
