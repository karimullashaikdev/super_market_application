package com.karim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.AuthResponse;
import com.karim.dto.LoginRequest;
import com.karim.dto.LoginResponse;
import com.karim.dto.RegisterRequest;
import com.karim.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService service;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

		String token = service.login(request);

		return ResponseEntity.ok(new LoginResponse(token));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		AuthResponse res = service.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(res);
	}

}
