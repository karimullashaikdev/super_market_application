package com.karim.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.karim.dto.AuthResponse;
import com.karim.dto.LoginRequest;
import com.karim.dto.RegisterRequest;
import com.karim.entity.User;
import com.karim.exception.DuplicateUserFoundException;
import com.karim.exception.InvalidCredentialsException;
import com.karim.repository.UserRepository;
import com.karim.service.AuthService;
import com.karim.service.EmailService;
import com.karim.util.JwtUtil;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserRepository repo;
	@Autowired
	private JwtUtil util;
	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	private EmailService emailService;

	@Override
	public AuthResponse register(RegisterRequest request) {
		if (!repo.existsByEmail(request.getEmail())) {
			User user = new User();
			BeanUtils.copyProperties(request, user);
			user.setPassword(encoder.encode(request.getPassword()));
			User newUser = repo.save(user);

			AuthResponse res = new AuthResponse();
			BeanUtils.copyProperties(newUser, res);

			// 2️⃣ Send welcome email
			emailService.sendWelcomeEmail(user.getEmail(), user.getName());
			return res;
		}
		throw new DuplicateUserFoundException("Duplicate Email : " + request.getEmail());
	}

	@Override
	public String login(LoginRequest request) {
		User user = repo.findByEmail(request.getEmail()).orElseThrow(
				() -> new UsernameNotFoundException("User not found with given Email : " + request.getEmail()));
		if (encoder.matches(request.getPassword(), user.getPassword())) {
			return util.generateToken(user.getEmail(), user.getRole());
		}
		throw new InvalidCredentialsException("Invalid Credentials !!!");
	}

}
