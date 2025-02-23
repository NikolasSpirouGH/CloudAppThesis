package com.cloud_ml_app_thesis.controller;


import com.cloud_ml_app_thesis.payload.ErrorResponse;
import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import com.cloud_ml_app_thesis.payload.response.DataMapResponse;
import com.cloud_ml_app_thesis.payload.response.InformationResponse;
import com.cloud_ml_app_thesis.payload.response.ObjectsDataResponse;
import com.cloud_ml_app_thesis.service.DatasetConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/dataset-configurations")
public class DatasetConfigurationController {
    private final DatasetConfigurationService datasetConfigurationService;

    @Autowired
    public DatasetConfigurationController(DatasetConfigurationService datasetConfigurationService){
        this.datasetConfigurationService = datasetConfigurationService;
    }


    @PostMapping("/upload-dataset-configuration")
    public ResponseEntity<CustomResponse> uploadDataset(@RequestParam("datasetId") String datasetId,
                                                        @RequestParam("username")String username,
                                                        @RequestParam("basicAttributesColumns") String basicAttributesColumns,
                                                        @RequestParam("targetClassColumn") String targetClassColumn){
        try {
            Integer datasetIdInteger = Integer.parseInt(datasetId);
            CustomResponse response = datasetConfigurationService.uploadDatasetConfiguration(datasetIdInteger, username,
                    basicAttributesColumns, targetClassColumn);
            if(response instanceof DataMapResponse){
                return ResponseEntity.ok().body((DataMapResponse) response);
            } else {
                return ResponseEntity.badRequest().body((ErrorResponse) response);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Unexpected Error, please contact the support."));
        }
    }

    @GetMapping("/get-dataset-configurations")
    public ResponseEntity<CustomResponse> getDatasetConfigurations(@RequestParam String username){
        CustomResponse response = datasetConfigurationService.getDatasetConfigurations(username);
        if(response instanceof ObjectsDataResponse){
            return ResponseEntity.ok().body((ObjectsDataResponse) response);
        } else if (response instanceof InformationResponse) {
            return ResponseEntity.ok().body((InformationResponse) response);
        }
        return ResponseEntity.internalServerError().body(new ErrorResponse("Unexpected while trying to fetch the Datasets."));

    }
}
