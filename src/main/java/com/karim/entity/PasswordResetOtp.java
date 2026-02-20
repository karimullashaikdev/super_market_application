package com.karim.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "password_reset_otp")
@Data
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String otp;

    private LocalDateTime expiry;

    private boolean verified;

    private boolean deleted = false;

}
