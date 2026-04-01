package com.karim.repository;

import com.karim.entity.DeliveryOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryOtpRepository extends JpaRepository<DeliveryOtp, Long> {

    Optional<DeliveryOtp> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
}
