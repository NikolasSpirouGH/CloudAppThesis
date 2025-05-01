package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.request.user.UserCreateRequest;
import com.cloud_ml_app_thesis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")

public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody UserCreateRequest createUserRequest) {
        userService.createUser(createUserRequest);
        return ResponseEntity.ok("User created.");
    }

}
