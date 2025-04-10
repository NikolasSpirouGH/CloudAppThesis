package com.cloud_ml_app_thesis.service.security;

import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
//    private final Argon2PasswordEncoder passwordEncoder;
    private final Argon2PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    //TODO Real Request object
    public String register(String username, String password, Set<Role> roles){
        if(userRepository.existsByUsername(username)){
            throw new RuntimeException("Username already taken!");
        }
        String hashedPassword = passwordEncoder.encode(password);
        //TODO fill the constructor
        User user = new User();
        userRepository.save(user);

        return jwtService.generateToken(
                user.getUsername(),
                user.getRoles().stream()
                .map(role -> role.getName().getAuthority()).collect(Collectors.toList())
        );
    }

    public String login(String username, String password){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        var userDetails = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return jwtService.generateToken(userDetails.getUsername(), roles);

    }
}
