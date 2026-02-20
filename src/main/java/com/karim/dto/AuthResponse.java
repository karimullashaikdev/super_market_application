package com.karim.dto;

import lombok.Data;

@Data
public class AuthResponse {

	private Long id;
	private String name;
	private String email;
	private String role;
}
