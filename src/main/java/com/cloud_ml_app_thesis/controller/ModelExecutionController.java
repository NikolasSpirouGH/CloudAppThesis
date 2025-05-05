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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prediction_results.arff")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(predictionFile.contentLength())
                    .body(predictionFile);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/prediction-result")
    public ResponseEntity<String> getPredictionResults(
            @RequestParam Integer modelId,
            @RequestParam Integer predictionFileId
    ) {
        try {
            String result = modelExecutionService.getPredictionResults(modelId, predictionFileId);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Execution not found.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }
    }



}
