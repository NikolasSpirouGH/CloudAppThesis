package com.backend.mlapp.controllers;

import com.backend.mlapp.entity.Training;
import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.payload.TrainingStatusResponse;
import com.backend.mlapp.service.UserService;
import com.backend.mlapp.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.backend.mlapp.entity.AppUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    private final UserService userService;

    @PostMapping("/train-model")
    public ResponseEntity<?> trainModel(Authentication authentication, @RequestBody TrainRequest trainRequest) {
        try {
            Integer userId = userService.getUserFromAuth(authentication);
            Optional<AppUser> user = userService.getUserById(userId);
            if (trainRequest.getFileReference() == null || trainRequest.getAlgorithm() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            CompletableFuture<String> trainingIdFuture = trainingService.trainModel(user.get(), trainRequest);

            return ResponseEntity.accepted().body(Map.of("message", "Training request submitted successfully", "trainingId", trainingIdFuture.get()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error starting training process: " + e.getMessage()));
        }
    }
    @GetMapping("/train-status/{trainingId}")
    public ResponseEntity<TrainingStatusResponse> getTrainingStatus(@PathVariable Integer trainingId) {
        TrainingStatusResponse response = trainingService.getTrainingStatus(trainingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-trainings")
    public List<Training> getTrainings(Authentication authentication,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       @RequestParam(required = false) String algorithm) {
        Integer userId = userService.getUserFromAuth(authentication);
        return trainingService.getTrainingsByCriteria(userId, startDate, endDate, algorithm);
    }

}




