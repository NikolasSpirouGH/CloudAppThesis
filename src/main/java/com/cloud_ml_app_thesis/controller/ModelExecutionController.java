package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.service.ModelExecutionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/model-exec")
@RequiredArgsConstructor
public class ModelExecutionController {

    private final ModelExecutionService modelExecutionService;

    @PostMapping("/execute")
    public ResponseEntity<Resource> executeModel(@RequestParam Integer modelId,
                                                 @RequestParam MultipartFile predictDataset) {
        try {
            Resource predictionFile = modelExecutionService.executeModel(modelId, predictDataset);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"predictions.arff\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(predictionFile);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/prediction-result")
    public ResponseEntity<Resource> getPredictionResults(
            @RequestParam Integer modelExecutionId
    ) {
        try {
            Resource result = modelExecutionService.getPredictionResults(modelExecutionId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"predictions.arff\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



}
