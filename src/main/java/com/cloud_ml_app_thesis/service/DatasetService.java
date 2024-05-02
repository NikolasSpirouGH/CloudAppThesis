package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis
.model.Dataset;
import com.cloud_ml_app_thesis
.model.DatasetConfiguration;
import com.cloud_ml_app_thesis.repository.DatasetConfigurationRepository;
import com.cloud_ml_app_thesis
.repository.DatasetRepository;
import com.cloud_ml_app_thesis.request.UploadDatasetConfigurationRequest;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RequiredArgsConstructor
@Service
public class DatasetService {
    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);

    private final DatasetRepository datasetRepository;
    private final DatasetConfigurationRepository datasetConfigurationRepository;
    public String uploadDataset(MultipartFile file){
        return null;
    }

    public String uploadDatasetConfiguration(UploadDatasetConfigurationRequest request){

        Dataset dataset = null;
        try {
            dataset = datasetRepository.findByFileUrl(request.getFileUrl()).orElseThrow(() -> new NotFoundException("Big Nikolaos"));
        } catch (NotFoundException e) {
            //to logaroume
            return "Dataset not Found.";
        }
        DatasetConfiguration datasetConfiguration = new DatasetConfiguration(null, request.getBasicAttributesColumns(), request.getTargetColumn(), dataset);
        try {
            DatasetConfiguration uploadedDatasetConfiguration = datasetConfigurationRepository.save(datasetConfiguration);
            log.info("Saved dataset configuration with ID {}", uploadedDatasetConfiguration.getId());
        } catch (DataAccessException e) {
        // Handle exceptions
            log.error("Failed to save dataset configuration", e);

            //TODO Throw Custom Exception that will be handled from @ControllerAdvisor Class
            //throw new CustomDatabaseException("Unable to save dataset configuration.", e);

            return "Failed to save dataset configuration: " + e.getMessage();
    }
        return null;
    }
}
