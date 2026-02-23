package com.karim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.karim.entity.PasswordResetOtp;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

	Optional<PasswordResetOtp> findByUserIdAndDeletedFalse(Long userId);

	@Modifying
	@Transactional
	@Query("UPDATE PasswordResetOtp o SET o.deleted = true WHERE o.userId = :userId")
	void softDeleteByUserId(@Param("userId") Long userId);
}
