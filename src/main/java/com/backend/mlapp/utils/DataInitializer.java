package com.backend.mlapp.utils;

import com.backend.mlapp.entity.Algorithm;
import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.enumeration.UserRole;
import com.backend.mlapp.enumeration.UserStatus;
import com.backend.mlapp.repository.AlgorithmRepository;
import com.backend.mlapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlgorithmRepository algorithmRepository;
    @Value("${ADMIN_PWD}")
    private String adminPassword;
    @Value("123456nik")
    private String userPassword;

    public void run(String... args) {
        List<AppUser> admins = List.of(
                new AppUser(null, "nikolas", "Spirou", "nikolas@gmail.com", passwordEncoder.encode(adminPassword), 27, "Senior SWE", "Greece", UserRole.ADMIN, UserStatus.ACTIVE,null),
                new AppUser(null, "Nikos", "Rizogiannis", "rizo@gmail.com", passwordEncoder.encode(adminPassword), 27, "Senior SWE", "Greece", UserRole.ADMIN, UserStatus.ACTIVE, null),
                new AppUser(null, "john", "kennedy", "john@gmail.com", passwordEncoder.encode(userPassword), 27, "Senior SWE", "Greece", UserRole.USER, UserStatus.ACTIVE, null)

        );

        admins.forEach(admin -> {
            userRepository.findByEmail(admin.getEmail())
                    .ifPresent(userRepository::delete);
            userRepository.save(admin);
        });

        System.out.println("Admins recreated.");

    }
}