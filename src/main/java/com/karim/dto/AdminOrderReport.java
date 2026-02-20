package com.karim.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminOrderReport {

    private Long orderId;
    private Long userId;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;

}
