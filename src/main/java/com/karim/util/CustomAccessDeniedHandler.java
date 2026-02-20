package com.karim.util;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
			throws IOException {

		String path = request.getRequestURI();

		String message;

		if (path.startsWith("/admin")) {
			message = "This API is only for ADMIN users";
		} else if (path.startsWith("/user")) {
			message = "This API is only for normal USERS";
		} else {
			message = "You are not allowed to access this resource";
		}

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json");

		response.getWriter().write("""
				{
				  "status": 403,
				  "message": "%s"
				}
				""".formatted(message));
	}
}
