package com.backend.mlapp.controllers;

import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.service.TrainingService;
import com.backend.mlapp.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    private final ObjectMapper objectMapper;

    @PostMapping("/train-model")
    public ResponseEntity<String> trainModel(@Valid @ModelAttribute TrainRequest trainRequest) throws Exception {

        System.out.println(trainRequest.getFile());
        if (trainRequest.getAlgorithmConfigs() != null) {
            Map<String, String> algorithmConfigs = objectMapper.readValue(trainRequest.getAlgorithmConfigs(), new TypeReference<>() {
            });
            trainRequest.setAlgorithmConfigs(objectMapper.writeValueAsString(algorithmConfigs));
        }
        CompletableFuture<String> trainingRequestId = trainingService.trainModel(trainRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("Training request submitted successfully. Training ID: " + trainingRequestId);
    }

/*    @PostMapping("/saveTraining")
    public ResponseEntity<String> saveModel(@RequestParam String trainingId) {
        try {
            // Assuming a service method that handles the logic
            String modelName = trainingService.saveModelToMinIO(trainingId);
            return ResponseEntity.ok("Model saved successfully as " + modelName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save the model: " + e.getMessage());
        }*/
    }




