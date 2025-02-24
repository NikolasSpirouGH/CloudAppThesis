package com.cloud_ml_app_thesis.controller.security;

import com.cloud_ml_app_thesis.payload.request.LoginRequest;
import com.cloud_ml_app_thesis.payload.request.UserRegistrationRequest;
import com.cloud_ml_app_thesis.payload.response.AuthResponse;
import com.cloud_ml_app_thesis.service.security.AuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request){
        String token = authService.register(request.username(), request.password(), null);
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @PostMapping("/register/organization")
    public ResponseEntity<?> registerOrganization(@RequestBody UserRegistrationRequest request){
        String token = authService.register(request.username(), request.password(), null);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }

}
