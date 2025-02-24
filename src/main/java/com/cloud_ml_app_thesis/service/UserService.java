package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;
import com.cloud_ml_app_thesis.enumeration.status.UserStatusEnum;
import com.cloud_ml_app_thesis.payload.request.CreateUserRequest;
import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import com.cloud_ml_app_thesis.payload.response.ErrorStatusResponse;
import com.cloud_ml_app_thesis.payload.response.SingleObjectDataResponse;
import com.cloud_ml_app_thesis.repository.RoleRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.repository.status.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserStatusRepository userStatusRepository;
    public CustomResponse createUser(CreateUserRequest request) {
        Optional<User> userExists = userRepository.findByEmail(request.getEmail());
        if(userExists.isPresent()) {
          return new ErrorStatusResponse("User already exists.", HttpStatus.BAD_REQUEST);
        }

        if(!request.getPassword().equals(request.getPasswordConfirmation())){
            return new ErrorStatusResponse("Password doesn't match with password confirmation.", HttpStatus.BAD_REQUEST);
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

        return new SingleObjectDataResponse(user, "OK");
    }
}
