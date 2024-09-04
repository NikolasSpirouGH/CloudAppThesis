package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.payload.ErrorResponse;
import com.cloud_ml_app_thesis.payload.request.CreateDatasetConfigurationRequest;
import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import com.cloud_ml_app_thesis.payload.response.DataResponse;
import com.cloud_ml_app_thesis.payload.response.InformationResponse;
import com.cloud_ml_app_thesis.payload.response.ObjectsDataResponse;
import com.cloud_ml_app_thesis.service.DatasetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/datasets")

public class DatasetController {


    private final DatasetService datasetService;

    @Autowired
    public DatasetController(DatasetService datasetService){
        this.datasetService = datasetService;
    }
    // TODO me jwt auth tha pairnoume to user einai gia testing auto
    @PostMapping("/upload-dataset")
    public ResponseEntity<CustomResponse> uploadDataset(@RequestPart("file") MultipartFile file, @RequestPart("username")String username) {
        try {
            CustomResponse response = datasetService.uploadDataset(file, username);
          if(response instanceof DataResponse){
              return ResponseEntity.ok().body((DataResponse) response);
          } else {
              return ResponseEntity.badRequest().body((ErrorResponse) response);
          }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Unexpected Error, please contact the support."));
        }
    }

    @GetMapping("/get-datasets")
    public ResponseEntity<CustomResponse> getDatasets(@RequestParam String username) {
        CustomResponse response = datasetService.getDatasets(username);
        if (response instanceof ObjectsDataResponse){
            return ResponseEntity.ok().body((ObjectsDataResponse) response);
        } else if (response instanceof InformationResponse){
            return ResponseEntity.ok().body((InformationResponse) response);
        }
        return ResponseEntity.internalServerError().body(new ErrorResponse("Unexpected while trying to fetch the Datasets."));

    }
    @GetMapping("/get-dataset-urls")
    public ResponseEntity<List<String>> getDatasetsUrls(@RequestParam String email){
        List<String> datasetUrls = datasetService.getDatasetUrls(email);
        System.out.println(datasetUrls);
        return ResponseEntity.ok(datasetUrls);
    }

    @PostMapping("/create-dataset-conf")
    public ResponseEntity<Map<String, Object>> datasetConfiguration(@Valid @RequestBody CreateDatasetConfigurationRequest request){
        Integer id = datasetService.datasetConfiguration(request);
        if(id != null){
            return ResponseEntity.ok().body(Collections.singletonMap("id", id));
        }
        return ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", "Couldn't create the configured dataset."));
    }

}
