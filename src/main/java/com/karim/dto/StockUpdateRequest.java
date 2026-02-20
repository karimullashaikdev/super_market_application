package com.karim.dto;

import lombok.Data;

@Data
public class StockUpdateRequest {

	private Long productId;

	private Integer quantity; // quantity to add or set

	private boolean setExact; // if true, set exact stock; else add to existing

	// getters / setters
}
