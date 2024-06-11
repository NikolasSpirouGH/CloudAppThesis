package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.entity.ModelExecution;
import com.cloud_ml_app_thesis.service.ModelExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-exec")
@RequiredArgsConstructor
public class ModelExecutionController {

    private final ModelExecutionService modelExecutionService;

    @PostMapping("/execute")
    public ResponseEntity<String> executeModel(@RequestParam Integer modelId,
                                                       @RequestParam Integer datasetId) {
        try {
            ModelExecution execution = modelExecutionService.executeModel(modelId, datasetId);
            return ResponseEntity.ok("Prediction OK: " + execution.getModel());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


}
