package com.karim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karim.entity.PaymentOtp;

public interface PaymentOtpRepository extends JpaRepository<PaymentOtp, Long> {

	Optional<PaymentOtp> findByOrderIdAndDeletedFalse(Long orderId);
}
