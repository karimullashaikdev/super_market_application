package com.karim.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.karim") // Only applies to your packages
public class GlobalExceptionHandler {

    // ---------------- User Not Found ----------------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    // ---------------- Invalid Credentials ----------------
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    // ---------------- Unauthorized Access ----------------
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    // ---------------- Cart Item Not Found ----------------
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<String> handleCartItemNotFound(CartItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    // ---------------- Out Of Stock ----------------
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<String> handleOutOfStock(OutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    // ---------------- Cart Item Removed ----------------
    @ExceptionHandler(CartItemRemovedException.class)
    public ResponseEntity<String> handleCartItemRemoved(CartItemRemovedException ex) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ex.getMessage());
    }

    // ---------------- Generic Exception ----------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip Swagger/OpenAPI endpoints
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/swagger-ui.html")) {
            throw new RuntimeException(ex); // Let Spring handle it
        }

        // Return structured JSON for other exceptions
        Map<String, Object> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("path", path);
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
