package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.AppUser;
import com.cloud_ml_app_thesis.enumeration.UserRole;
import com.cloud_ml_app_thesis.enumeration.UserStatus;
import com.cloud_ml_app_thesis.payload.CreateUserRequest;
import com.cloud_ml_app_thesis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void createUser(CreateUserRequest createUserRequest) {
        Optional<AppUser> userExists = userRepository.findByEmail(createUserRequest.getEmail());
        if(userExists.isPresent()) {
            System.out.println("User already exists.");;
        }

        var user = AppUser.builder()
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .password(createUserRequest.getPassword())
                .country(createUserRequest.getCountry())
                .email(createUserRequest.getEmail())
                .profession(createUserRequest.getProfession())
                .age(createUserRequest.getAge())
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .build();
        user = userRepository.save(user);
    }
}
