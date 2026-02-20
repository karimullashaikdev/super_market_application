package com.karim.service;

import java.util.List;

import com.karim.entity.User;

public interface UserService {
	Long getCurrentUserId();

	String getCurrentUserEmail();
	
	List<User> getAllUsers();
}
