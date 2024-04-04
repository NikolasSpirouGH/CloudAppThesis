package com.backend.mlapp.service;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.payload.TokenRequest;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {

    void sendVerificationEmail(AppUser user);

    String verifyCodeFromEmail(TokenRequest verificationToken);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword);
}
