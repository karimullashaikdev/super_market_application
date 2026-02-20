package com.karim.dto;

import lombok.Data;

@Data
public class ItemSalesResponse {

    private Long productId;
    private String productName;
    private Long quantitySold;
    private Double revenue;
}
