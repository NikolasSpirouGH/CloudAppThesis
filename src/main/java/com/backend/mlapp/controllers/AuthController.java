package com.backend.mlapp.controllers;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.payload.AuthResponse;
import com.backend.mlapp.payload.LoginRequest;
import com.backend.mlapp.payload.RegisterRequest;
import com.backend.mlapp.repository.UserRepository;
import com.backend.mlapp.service.AuthService;
import com.backend.mlapp.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest){
        AppUser appUser = authService.register(registerRequest);
        return new ResponseEntity<>("User registered Successfuly " + appUser.getEmail(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.login(loginRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        emailService.sendPasswordResetEmail(email);
        return new ResponseEntity("Password reset email sent successfully.", HttpStatus.OK);
    }
}
