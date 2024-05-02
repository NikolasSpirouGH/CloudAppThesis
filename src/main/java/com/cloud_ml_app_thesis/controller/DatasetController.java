package com.cloud_ml_app_thesis.controller;


import com.cloud_ml_app_thesis.request.UploadDatasetConfigurationRequest;
import com.cloud_ml_app_thesis.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/datasetWS")

@Slf4j
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    //Upload apla ena dataset to path
    @PostMapping("/uploadDataset")
    public ResponseEntity<String> uploadDataset(@RequestParam MultipartFile file){
        String fileUrl = datasetService.uploadDataset(file);
        return null;
    }


    //Epistrefei ola ta urls twn dataset
    @GetMapping("/getDatasets")
    public ResponseEntity<List<String>> getDatasets(@RequestParam String username){
        return null;
    }

    //DATASET CONFIGURATION *******************************
    @PostMapping("/uploadDatasetConfiguration")
    public ResponseEntity<String> uploadDatasetConfiguration(UploadDatasetConfigurationRequest request){

        return null;
    }



}
