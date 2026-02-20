package com.karim.exception;

public class DuplicateUserFoundException extends RuntimeException {

	public DuplicateUserFoundException(String msg) {
		super(msg);
	}
}
