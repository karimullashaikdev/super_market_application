package com.karim.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {

    private Long orderId;

    private String otp;
}
