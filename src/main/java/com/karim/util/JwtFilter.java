//package com.karim.util;
//
//import java.io.IOException;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//	@Autowired
//	private JwtUtil util;
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//		String authHeader = request.getHeader("Authorization");
//		String token = null;
//		String username = null;
//		String role = null;
//
//		if (authHeader != null && authHeader.startsWith("Bearer ")) {
//			token = authHeader.substring(7);
//			username = util.extractUsername(token);
//			role = util.extractRole(token);
//
//			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//				List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+role));
//				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
//						authorities);
//
//				SecurityContextHolder.getContext().setAuthentication(auth);
//			}
//		}
//		filterChain.doFilter(request, response);
//	}
//
//}

package com.karim.util;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil util;

	// ---------------- Skip Swagger and public endpoints ----------------
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
				|| path.startsWith("/swagger-resources") || path.startsWith("/webjars")
				|| path.equals("/swagger-ui.html") || path.equals("/login") || path.equals("/register")
				|| path.startsWith("/api/auth/forgot-password") || path.startsWith("/api/auth/reset-password")
				|| path.startsWith("/api/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				String username = util.extractUsername(token);
				String role = util.extractRole(token);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
							authorities);
					SecurityContextHolder.getContext().setAuthentication(auth);
				}

			} catch (ExpiredJwtException ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"error\":\"Token expired → user must log in again.\"}");
				response.getWriter().flush();
				return; // stop filter chain
			} catch (Exception ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"error\":\"Invalid token.\"}");
				response.getWriter().flush();
				return; // stop filter chain
			}
		}

		filterChain.doFilter(request, response);
	}
}