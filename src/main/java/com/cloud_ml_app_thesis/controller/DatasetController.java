package com.cloud_ml_app_thesis.controller;


import com.cloud_ml_app_thesis.entity.Dataset;
import com.cloud_ml_app_thesis.payload.DatasetConfigurationRequest;
import com.cloud_ml_app_thesis.service.DatasetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/datasets")

@Slf4j
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    // TODO me jwt auth tha pairnoume to user einai gia testing auto
    @PostMapping("/upload-dataset")
    public ResponseEntity<String> uploadDataset(@RequestParam MultipartFile file, @RequestParam String email){
        String fileUrl = datasetService.uploadDataset(file, email);
        return ResponseEntity.ok("Upload succeed." + fileUrl.toString());
    }

    @GetMapping("/get-datasets")
    public ResponseEntity<List<Dataset>> getDatasets(@RequestParam String email){
        List<Dataset> datasetUrls = datasetService.getDatasetUrls(email);
        System.out.println(datasetUrls);
        return ResponseEntity.ok(datasetUrls);
    }

    @PostMapping("/dataset-conf")
    public ResponseEntity<String> datasetConfiguration(@Valid @RequestBody DatasetConfigurationRequest request){
        datasetService.datasetConfiguration(request);
        return ResponseEntity.ok("Dataset configured.");
    }



}
