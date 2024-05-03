package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.AppUser;
import com.cloud_ml_app_thesis.entity.Dataset;
import com.cloud_ml_app_thesis.entity.DatasetConfiguration;
import com.cloud_ml_app_thesis.payload.DatasetConfigurationRequest;
import com.cloud_ml_app_thesis.repository.DatasetConfigurationRepository;
import com.cloud_ml_app_thesis.repository.DatasetRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DatasetService {
    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);

    private final DatasetRepository datasetRepository;
    private final DatasetConfigurationRepository datasetConfigurationRepository;
    private final UserRepository userRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("ml-datasets")
    private String bucketName;

    @Value("http://192.168.1.6:9000")
    private String minioUrl;

    public String uploadDataset(MultipartFile file, String email) {
        try {
            //Xrisimopoioume thn putObject anti gia upload object otan theloume na anevasoume olo to arxeio
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            Dataset dataset = new Dataset();
            dataset.setFileUrl(minioUrl + "/" + bucketName + "/" + file.getOriginalFilename());
            String fileUrl = dataset.getFileUrl();
            Optional<AppUser> user = userRepository.findByEmail(email);
            //Tha pairnei ton user me authentication apo to jwt automata
            dataset.setUser(user.get());
            dataset.setUploadedDateTime(LocalDateTime.now());
            dataset.setFileName(file.getOriginalFilename());

            if(datasetRepository.findByFileUrl(fileUrl).isPresent()) {
                //TODO CUSTOM EXCEPTION AND LOGGING
                throw new IllegalArgumentException();
            }
            datasetRepository.save(dataset);
            return "File uploaded successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during file upload: " + e.getMessage();
        }
    }


    public String datasetConfiguration(DatasetConfigurationRequest request){
        Dataset dataset = null;
        try {
            dataset = datasetRepository.findByFileUrl(request.getFileUrl()).orElseThrow(() -> new NotFoundException("..."));
        } catch (NotFoundException e) {
            return "Dataset not Found.";
        }
        DatasetConfiguration datasetConfiguration = new DatasetConfiguration(null, request.getBasicAttributesColumns(), request.getTargetColumn(), dataset);
        try {
            DatasetConfiguration uploadedDatasetConfiguration = datasetConfigurationRepository.save(datasetConfiguration);
            log.info("Saved dataset configuration with ID {}", uploadedDatasetConfiguration.getId());
        } catch (DataAccessException e) {
            log.error("Failed to save dataset configuration", e);

            //TODO Throw Custom Exception that will be handled from @ControllerAdvisor Class
            //throw new CustomDatabaseException("Unable to save dataset configuration.", e);

            return "Failed to save dataset configuration: " + e.getMessage();
    }
        return null;
    }

    public List<Dataset> getDatasetUrls(String email) {
        Optional<AppUser> user = userRepository.findByEmail(email);
        if(!user.isPresent()) {
            //TODO LOGGER AND EXCEPTION HANDLING
            System.out.println("User do not exists");
        }
        return user.get().getDatasets();
    }
}
