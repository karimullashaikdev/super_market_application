package com.karim.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karim.config.AuditorConfig;
import com.karim.entity.User;
import com.karim.repository.UserRepository;
import com.karim.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository repo;

	@Override
	public Long getCurrentUserId() {
		String email = AuditorConfig.getCurrentUserEmail();

		if (email == null) {
			throw new RuntimeException("User not authenticated");
		}

		User user = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));

		return user.getId();
	}
	
	@Override
	public String getCurrentUserEmail() {
		String email = AuditorConfig.getCurrentUserEmail();

		return email;
	}

	@Override
	public List<User> getAllUsers() {
		return repo.findAll();
	}

}
