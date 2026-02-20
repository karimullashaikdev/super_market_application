package com.karim.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AuditorConfig {

	@Bean
	public AuditorAware<String> auditProvider() {
		return () -> {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.isAuthenticated()) {
				return Optional.of("SYSTEM");
			}
			return Optional.of(auth.getName());
		};
	}

	public static String getCurrentUserEmail() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}

		return auth.getName();
	}
}
