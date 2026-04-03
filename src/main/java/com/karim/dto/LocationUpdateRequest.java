package com.karim.dto;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private Long    orderId;
    private Double  latitude;
    private Double  longitude;
}