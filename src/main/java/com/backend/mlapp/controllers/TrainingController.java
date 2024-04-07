package com.backend.mlapp.controllers;

import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.payload.TrainingStatusResponse;
import com.backend.mlapp.utils.FileStorageService;
import com.backend.mlapp.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileReference = fileStorageService.uploadFile(file);
            return ResponseEntity.ok(Map.of("message", "File uploaded successfully", "fileReference", fileReference));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/train-model")
    public ResponseEntity<?> trainModel(@RequestBody TrainRequest trainRequest) {
        try {
            if (trainRequest.getFileReference() == null || trainRequest.getAlgorithm() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            CompletableFuture<String> trainingIdFuture = trainingService.trainModel(trainRequest);

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


}




