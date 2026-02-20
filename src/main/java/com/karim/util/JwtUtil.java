//package com.karim.util;
//
//import java.util.Date;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//
//@Component
//public class JwtUtil {
//
//	@Value("${jwt.secret}")
//	private String secret;
//	@Value("${jwt.expiration}")
//	private Long expiration;
//	
//	public String generateToken(String username,String role) {
//		return Jwts.builder()
//				.setSubject(username)
//				.claim("role", role)
//				.setIssuedAt(new Date(System.currentTimeMillis()))
//				.setExpiration(new Date(System.currentTimeMillis()+expiration))
//				.signWith(SignatureAlgorithm.HS256, secret)
//				.compact();
//	}
//	
//	public String extractUsername(String token) {
//		return Jwts.parser()
//				.setSigningKey(secret)
//				.parseClaimsJws(token)
//				.getBody()
//				.getSubject();
//	}
//	
//	public String extractRole(String token) {
//		return Jwts.parser()
//				.setSigningKey(secret)
//				.parseClaimsJws(token)
//				.getBody()
//				.get("role",String.class);
//	}
//}
package com.karim.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	// ----------------- Generate Token -----------------
	public String generateToken(String username, String role) {
		return Jwts.builder().setSubject(username).claim("role", role).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	// ----------------- Extract Username -----------------
	public String extractUsername(String token) {
		return getClaims(token).getSubject();
	}

	// ----------------- Extract Role -----------------
	public String extractRole(String token) {
		return getClaims(token).get("role", String.class);
	}

	// ----------------- Validate Token -----------------
	public boolean isTokenValid(String token) {
		try {
			Claims claims = getClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (ExpiredJwtException ex) {
			// Token expired
			return false;
		} catch (Exception ex) {
			// Token invalid
			return false;
		}
	}

	// ----------------- Get Claims -----------------
	private Claims getClaims(String token) {
		try {
			return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException ex) {
			throw ex; // Let caller handle expired token
		} catch (SignatureException ex) {
			throw new RuntimeException("Invalid JWT signature");
		} catch (Exception ex) {
			throw new RuntimeException("Invalid JWT token");
		}
	}

	// ----------------- Check if Token Expired -----------------
	public boolean isTokenExpired(String token) {
		try {
			Date expirationDate = getClaims(token).getExpiration();
			return expirationDate.before(new Date());
		} catch (ExpiredJwtException ex) {
			return true; // Already expired
		} catch (Exception ex) {
			return true; // Invalid token treated as expired
		}
	}
}
