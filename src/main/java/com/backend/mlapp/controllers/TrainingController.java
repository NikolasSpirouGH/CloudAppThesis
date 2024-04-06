package com.backend.mlapp.controllers;

import com.backend.mlapp.exception.ConfigurationParseException;
import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.payload.TrainingStatusResponse;
import com.backend.mlapp.service.TrainingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.mlapp.exception.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    private final ObjectMapper objectMapper;

    @PostMapping("/train-model")
    public ResponseEntity<String> trainModel(@Valid @ModelAttribute TrainRequest trainRequest){
        CompletableFuture<String> trainingRequestId;
        try {
            if (trainRequest.getAlgorithmConfigs() != null) {
                Map<String, String> algorithmConfigs;
                try {
                    algorithmConfigs = objectMapper.readValue(trainRequest.getAlgorithmConfigs(), new TypeReference<>() {});
                } catch (JsonProcessingException e) {
                    throw new ConfigurationParseException("Failed to parse algorithm configs", e);
                }
                trainRequest.setAlgorithmConfigs(objectMapper.writeValueAsString(algorithmConfigs));
            }
            trainingRequestId = trainingService.trainModel(trainRequest);
        } catch (TrainingModelException e) {
            throw e;
        } catch (Exception e){
            throw new TrainingModelException("Unexpected error occurred while training the model");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Training request submitted successfully. Training ID: " + trainingRequestId.join());
    }

    @GetMapping("/train-status/{trainingId}")
    public ResponseEntity<TrainingStatusResponse> getTrainingStatus(@PathVariable Integer trainingId) {
        TrainingStatusResponse response = trainingService.getTrainingStatus(trainingId);
        return ResponseEntity.ok(response);
    }
}




