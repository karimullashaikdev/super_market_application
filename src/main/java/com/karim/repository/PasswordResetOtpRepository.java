package com.karim.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.karim.entity.PasswordResetOtp;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByUserIdAndDeletedFalse(Long userId);
}
