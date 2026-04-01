package com.karim.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AddressResponseDto {

    private Long id;
    private String label;
    private String name;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pin;
    private String landmark;
    private Boolean isDefault;
    private String formattedAddress;   // ready-made string for checkout
    private LocalDateTime createdAt;
}