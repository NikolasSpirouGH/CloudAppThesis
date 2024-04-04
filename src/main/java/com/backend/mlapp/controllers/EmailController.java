package com.backend.mlapp.controllers;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.entity.Token;
import com.backend.mlapp.payload.TokenRequest;
import com.backend.mlapp.repository.UserRepository;
import com.backend.mlapp.repository.TokenRepository;
import com.backend.mlapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/verify-register")
    public ResponseEntity<String> verifyAccount(@RequestBody TokenRequest verificationRequest) {
        String message = emailService.verifyCodeFromEmail(verificationRequest);

        if ("Account verified successfully.".equals(message)) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        resetPassword(token,newPassword);
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}
