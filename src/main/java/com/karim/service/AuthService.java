package com.karim.service;

import com.karim.dto.AuthResponse;
import com.karim.dto.LoginRequest;
import com.karim.dto.RegisterRequest;

public interface AuthService {
	
	AuthResponse register(RegisterRequest request);
	String login(LoginRequest request);
}
