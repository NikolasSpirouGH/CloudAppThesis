package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_ml_app_thesis.config.security.JwtTokenProvider;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private RoleRepository roleRepository;

//    @Autowired
//    private StatusRepository statusRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeRolesAndStatuses() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role with full access");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("User role with limited access");
            roleRepository.save(userRole);
        }

        if (statusRepository.count() == 0) {
            Status activeStatus = new Status();
            activeStatus.setName("ACTIVE");
            activeStatus.setDescription("Account is active");
            statusRepository.save(activeStatus);

            Status inactiveStatus = new Status();
            inactiveStatus.setName("INACTIVE");
            inactiveStatus.setDescription("Account is inactive");
            statusRepository.save(inactiveStatus);
        }
    }

    @PostMapping("/register")
    public String registerAccount(@RequestBody UserRegistrationRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role role = roleRepository.findById(registerRequest.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID"));

        Status status = statusRepository.findById(0L)
                .orElseThrow(() -> new IllegalArgumentException("Invalid status ID"));

        Account account = new Account();
        account.setEmail(registerRequest.email());
        account.setPassword(passwordEncoder.encode(registerRequest.password()));
        account.setRole(role);
        account.setStatus(status);

        accountRepository.save(account);
        return "Account registered successfully";
    }

    @PostMapping("/login")
    public String authenticateAccount(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }
}