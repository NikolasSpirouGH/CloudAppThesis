package com.backend.mlapp.controllers;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.service.DatasetService;
import com.backend.mlapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final UserService userService;

    private final DatasetService datasetService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(Authentication authentication, @RequestParam("file") MultipartFile file) {
        try {
            Integer userId = userService.getUserFromAuth(authentication);
            Optional<AppUser> user = userService.getUserById(userId);
            String fileReference = datasetService.uploadFile(userId, file);
            return ResponseEntity.ok(Map.of("message", "File uploaded successfully by " + user.get().getEmail(), "fileReference", fileReference));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload file: " + e.getMessage()));
        }
    }
}
