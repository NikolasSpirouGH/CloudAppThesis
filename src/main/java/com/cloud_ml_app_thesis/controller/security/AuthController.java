package com.cloud_ml_app_thesis.controller.security;

import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.status.UserStatus;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;
import com.cloud_ml_app_thesis.enumeration.status.UserStatusEnum;
import com.cloud_ml_app_thesis.dto.request.user.LoginRequest;
import com.cloud_ml_app_thesis.dto.request.user.UserRegisterRequest;
import com.cloud_ml_app_thesis.repository.RoleRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.repository.status.UserStatusRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_ml_app_thesis.config.security.JwtTokenProvider;

import java.util.Set;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    
    private final AuthenticationManager authenticationManager;

    
    private final UserRepository userRepository;

    
    private final RoleRepository roleRepository;

    
    private final UserStatusRepository userStatusRepository;

    
    private final JwtTokenProvider tokenProvider;

    
    private final Argon2PasswordEncoder passwordEncoder;

//    @PostConstruct
//    public void initializeRolesAndStatuses() {
//        if (roleRepository.count() == 0) {
//            Role adminRole = new Role();
//            adminRole.setName("ADMIN");
//            adminRole.setDescription("Administrator role with full access");
//            roleRepository.save(adminRole);
//
//            Role userRole = new Role();
//            userRole.setName("USER");
//            userRole.setDescription("User role with limited access");
//            roleRepository.save(userRole);
//        }
//
//        if (userStatusRepository.count() == 0) {
//            Status activeStatus = new Status();
//            activeStatus.setName("ACTIVE");
//            activeStatus.setDescription("Account is active");
//            statusRepository.save(activeStatus);
//
//            Status inactiveStatus = new Status();
//            inactiveStatus.setName("INACTIVE");
//            inactiveStatus.setDescription("Account is inactive");
//            statusRepository.save(inactiveStatus);
//        }
//    }

    @PostMapping("/register")
    public ResponseEntity<String> registerAccount(@RequestBody UserRegisterRequest registerRequest) {
        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new IllegalArgumentException("Your password doesn't match your password confirmation.");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already in use.");
        }

        if (userRepository.existsByEmail(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        Role role = roleRepository.findByName(UserRoleEnum.USER)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID."));

        UserStatus status = userStatusRepository.findByName(UserStatusEnum.INACTIVE).orElseThrow(() -> new EntityNotFoundException("Could not find Inactive Status."));
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(role));
        user.setStatus(status);
        user.setAge(registerRequest.getAge());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setCountry(registerRequest.getCountry());
        user.setProfession(registerRequest.getProfession());

//        Account account = new Account();
//        account.setEmail(registerRequest.email());
//        account.setPassword(passwordEncoder.encode(registerRequest.password()));
//        account.setRole(role);
//        account.setStatus(status);

        userRepository.save(user);
        return new ResponseEntity<>("Account registered successfully", HttpStatus.OK);
    }

    @PostMapping("/login")
    public String authenticateAccount(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }
}