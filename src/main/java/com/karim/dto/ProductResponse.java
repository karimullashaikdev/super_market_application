package com.karim.dto;

import lombok.Data;

@Data
public class ProductResponse {

	private Long id;
	private String name;
	private String category;
	private Double price;
	private int stock;
	private String brand;
}
