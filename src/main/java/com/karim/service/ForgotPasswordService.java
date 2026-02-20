package com.karim.service;

import com.karim.dto.ForgotPasswordRequest;
import com.karim.dto.ResetPasswordRequest;

public interface ForgotPasswordService {

    void requestPasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
