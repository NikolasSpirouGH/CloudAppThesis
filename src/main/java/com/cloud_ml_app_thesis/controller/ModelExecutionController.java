package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.response.GenericResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import com.cloud_ml_app_thesis.service.ModelExecutionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/model-exec")
@RequiredArgsConstructor
public class ModelExecutionController {

    private final ModelExecutionService modelExecutionService;

    @PostMapping("/execute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse<ByteArrayResource>> executeModel(@AuthenticationPrincipal UserDetails userDetails, @NotBlank @PathVariable Integer modelId,
                                                           @NotNull @RequestParam MultipartFile predictDataset, @RequestParam String targetUsername) {
        try {
            ByteArrayResource predictionFile = modelExecutionService.executeModel(userDetails, modelId, predictDataset, targetUsername);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"predictions.arff\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new GenericResponse<ByteArrayResource>(predictionFile, null, null, new Metadata()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/prediction-result")
    public ResponseEntity<GenericResponse<ByteArrayResource>> getPredictionResults(
            @RequestParam Integer modelId,
            @RequestParam Integer predictionFileId
    ) {
            ByteArrayResource result = modelExecutionService.getPredictionResults(modelId);
            return ResponseEntity.ok(new GenericResponse<>(result, null, null, new Metadata()));

    }



}
