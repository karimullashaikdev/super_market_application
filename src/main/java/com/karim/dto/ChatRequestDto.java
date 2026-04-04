package com.karim.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ChatRequestDto {
	private Long orderId;
	private String message;
	private List<Map<String, String>> history; // previous messages for context
	// getters + setters
}
