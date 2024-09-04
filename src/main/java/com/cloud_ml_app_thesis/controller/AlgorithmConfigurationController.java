package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.payload.request.CreateAlgorithmConfigurationRequest;
import com.cloud_ml_app_thesis.service.AlgorithmConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/algorithm-configurations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AlgorithmConfigurationController {

    private final AlgorithmConfigurationService algorithmConfigurationService;

    @PostMapping("/create-algorithm-configuration")
    public ResponseEntity<Map<String, Object>> createAlgorithmConfiguration(@RequestBody CreateAlgorithmConfigurationRequest request) {

        Integer id = algorithmConfigurationService.createAlgorithmConfiguration(request.getAlgorithmId(), request.getOptions());
        if(id != null) {
            return ResponseEntity.ok(Collections.singletonMap("id", id));
        } else {
            return  ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", "Error creating algorithm configuration."));
        }
    }


}
