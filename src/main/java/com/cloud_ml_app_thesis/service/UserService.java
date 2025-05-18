package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.request.user.UserCreateRequest;
import com.cloud_ml_app_thesis.dto.response.MyResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;
import com.cloud_ml_app_thesis.enumeration.status.UserStatusEnum;
import com.cloud_ml_app_thesis.repository.RoleRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.repository.status.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserStatusRepository userStatusRepository;
    public MyResponse<?> createUser(UserCreateRequest request) {
        Optional<User> userExists = userRepository.findByEmail(request.getEmail());
        if(userExists.isPresent()) {
          return new MyResponse<>("User already exists.", null, null, new Metadata());
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            return new MyResponse("Password doesn't match with password confirmation.",null, null, new Metadata());
        }

        Role userRole = roleRepository.findByName(UserRoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("Unexpected Error. If this error keep occurs, please contact the support."));

        com.cloud_ml_app_thesis.entity.status.UserStatus userStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Unexpected Error. If this error keep occurs, please contact the support."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(request.getPassword())
                .country(request.getCountry())
                .email(request.getEmail())
                .profession(request.getProfession())
                .age(request.getAge())
                .roles(Set.of(userRole))
                .status(userStatus)
                .build();
        user = userRepository.save(user);

        return new MyResponse<>(user, "OK", null, new Metadata());
    }
}
