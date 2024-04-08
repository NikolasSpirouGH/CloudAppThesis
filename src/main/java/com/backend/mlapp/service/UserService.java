package com.backend.mlapp.service;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.payload.UpdateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface UserService {

    AppUser updateUserInfo(UpdateRequest updateRequest, Integer id);

    AppUser updateMyInfo(UpdateRequest updateRequest);

    Integer getUserFromAuth(Authentication authentication);

    Optional<AppUser> getUserById(Integer userId);
}
