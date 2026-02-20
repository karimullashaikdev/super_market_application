package com.karim.service;

import com.karim.dto.PaymentRequest;
import com.karim.dto.OtpVerifyRequest;

public interface PaymentService {

    void initiatePayment(Long userId, PaymentRequest request);

    void verifyOtp(Long userId, OtpVerifyRequest request);
}
