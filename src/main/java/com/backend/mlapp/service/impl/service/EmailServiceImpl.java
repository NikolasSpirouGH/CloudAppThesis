package com.backend.mlapp.service.impl.service;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.entity.Token;
import com.backend.mlapp.enumeration.UserStatus;
import com.backend.mlapp.exception.ResourceNotFoundException;
import com.backend.mlapp.payload.TokenRequest;
import com.backend.mlapp.repository.UserRepository;
import com.backend.mlapp.repository.TokenRepository;
import com.backend.mlapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void sendVerificationEmail(AppUser user) {

        String token = UUID.randomUUID().toString();
        Token verificationToken = new Token();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + 86400000));
        tokenRepository.save(verificationToken);
        SimpleMailMessage mailMessage = getSimpleMailMessage(user, token);
        mailSender.send(mailMessage);
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        Optional<AppUser> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("The user does not exist.");
        }
        String token = UUID.randomUUID().toString();
        Token resetToken = new Token();
        resetToken.setToken(token);
        resetToken.setUser(userOptional.get());
        resetToken.setExpiryDate(new Date(System.currentTimeMillis() + 86400000));
        tokenRepository.save(resetToken);
        SimpleMailMessage mailMessage = getSimpleMailMessage(userOptional.get(), token);
        mailSender.send(mailMessage);
    }

    @NotNull
    private static SimpleMailMessage getSimpleMailMessage(AppUser user, String token) {
        String subject = "Account Verification";
        String verificationUrl = "Enter the following token on the verification page: " + token;
        String message = "Hello " + user.getFirstName() + ",\n\n" + "Please verify your account by entering the token below on our website:\n\n" + verificationUrl + "\n\nThank you!";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        return mailMessage;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Token resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            throw new ResourceNotFoundException("Reset token has expired or does not exist.");
        }
        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    @Override
    public String verifyCodeFromEmail(TokenRequest tokenRequest) {
        String token = tokenRequest.getToken();
        Token verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return "Invalid verification token.";
        }
        if (verificationToken.getExpiryDate().before(new Date())) {
            return "Verification token has expired.";
        }
        AppUser user = verificationToken.getUser();
        if (user == null) {
            return "No user found associated with this token.";
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);
        return "Account verified successfully.";
    }
}

