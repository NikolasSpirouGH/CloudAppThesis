package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.AppUser;
import com.cloud_ml_app_thesis.entity.Dataset;
import com.cloud_ml_app_thesis.entity.DatasetConfiguration;
import com.cloud_ml_app_thesis.payload.DatasetConfigurationRequest;
import com.cloud_ml_app_thesis.repository.DatasetConfigurationRepository;
import com.cloud_ml_app_thesis.repository.DatasetRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import io.minio.GetObjectArgs;
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
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.cloud_ml_app_thesis.exception.FileProcessingException;

@RequiredArgsConstructor
@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetConfigurationRepository datasetConfigurationRepository;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);

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

    public void datasetConfiguration(DatasetConfigurationRequest request){
        Dataset dataset = null;
        try {
            dataset = datasetRepository.findById(request.getDatasetId()).orElseThrow(() -> new NotFoundException("..."));
        } catch (NotFoundException e) {
            return;
        }
        DatasetConfiguration datasetConfiguration = new DatasetConfiguration(null, request.getBasicAttributesColumns(), request.getTargetColumn(), dataset);
        try {
            DatasetConfiguration uploadedDatasetConfiguration = datasetConfigurationRepository.save(datasetConfiguration);
            logger.info("Saved dataset configuration with ID {}", uploadedDatasetConfiguration.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to save dataset configuration", e);
            //TODO Throw Custom Exception that will be handled from @ControllerAdvisor Class
            e.getMessage();
        }
    }

    public List<String> getDatasetUrls(String email) {
        Optional<AppUser> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            //TODO LOGGER AND EXCEPTION HANDLING
            System.out.println("User do not exists");
        }
        return user.map(u -> u.getDatasets().stream()
                        .map(Dataset::getFileUrl)
                        .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
    }

    public DatasetConfiguration getDatasetConfiguration(Integer datasetConfId) throws Exception {
        return datasetConfigurationRepository.findById(datasetConfId)
                .orElseThrow(() -> new Exception("Dataset configuration ID " + datasetConfId + " not found."));
    }

    public Instances loadDataset(DatasetConfiguration datasetConfiguration) throws Exception {
        URI datasetUri = new URI(datasetConfiguration.getDataset().getFileUrl());
        logger.info("Dataset URI: {}", datasetUri);
        String bucketName = Paths.get(datasetUri.getPath()).getName(0).toString();
        String objectName = Paths.get(datasetUri.getPath()).subpath(1, Paths.get(datasetUri.getPath()).getNameCount()).toString();
        logger.info("Bucket Name: {}", bucketName);
        logger.info("Object Name: {}", objectName);

        InputStream datasetStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        logger.info("Dataset Stream obtained successfully.");

        // Convert the dataset to ARFF format if it is in CSV or Excel format
        String fileExtension = getFileExtension(objectName);
        if (fileExtension.equalsIgnoreCase(".csv")) {
            String arffFilePath = csvToArff(datasetStream, objectName);
            datasetStream = Files.newInputStream(Paths.get(arffFilePath));
        }

        Instances data = new ConverterUtils.DataSource(datasetStream).getDataSet();
        int prediction = 0;
        data = selectColumns(data, datasetConfiguration.getBasicAttributesColumns(), datasetConfiguration.getTargetColumn(), prediction);
        return data;
    }

    public Instances selectColumns(Instances data, String basicAttributesColumns, String targetClassColumn, int prediction) throws Exception {
        List<String> columnNames = new ArrayList<>();

        // Log the original dataset attributes
        logger.info("Original dataset attributes: ");
        for (int i = 0; i < data.numAttributes(); i++) {
            logger.info("Attribute {}: {}", i + 1, data.attribute(i).name());
        }

        // Default to all columns except the last one if basic attributes columns are not provided
        if (basicAttributesColumns == null || basicAttributesColumns.isEmpty()) {
            for (int i = 0; i < data.numAttributes(); i++) {
                columnNames.add(data.attribute(i).name());
            }
        } else {
            for (String index : basicAttributesColumns.split(",")) {
                columnNames.add(data.attribute(Integer.parseInt(index) - 1).name());
            }
        }
        logger.info("Selected basic attributes columns: {}", columnNames);

        if (prediction == 0) { // Training
            // Default to the last column if target class column is not provided
            if (targetClassColumn == null || targetClassColumn.isEmpty()) {
                logger.info("I am in train");
                targetClassColumn = data.attribute(data.numAttributes() - 1).name();
            } else {
                logger.info("i am in else");
                targetClassColumn = data.attribute(Integer.parseInt(targetClassColumn) - 1).name();
            }
            logger.info("Target class column: {}", targetClassColumn);

            // Ensure the target class column is included in the selection
            if (!columnNames.contains(targetClassColumn)) {
                columnNames.add(targetClassColumn);
            }
        }

        logger.info("Final columns to keep: {}", columnNames);

        // Create a list of attribute indices to keep
        List<Integer> indicesToKeep = new ArrayList<>();
        for (int i = 0; i < data.numAttributes(); i++) {
            if (columnNames.contains(data.attribute(i).name())) {
                indicesToKeep.add(i);
            }
        }
        logger.info("Indices to keep: {}", indicesToKeep);

        // Configure the Remove filter
        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(indicesToKeep.stream().mapToInt(i -> i).toArray());
        removeFilter.setInvertSelection(true); // Keep the specified indices
        removeFilter.setInputFormat(data);

        // Apply the filter to get the selected columns
        Instances filteredData = Filter.useFilter(data, removeFilter);

        if (prediction == 0) { // Training
            // Get the correct index for the target class column after filtering
            int targetIndex = -1;
            for (int i = 0; i < filteredData.numAttributes(); i++) {
                if (filteredData.attribute(i).name().equals(targetClassColumn)) {
                    targetIndex = i;
                    break;
                }
            }
            if (targetIndex == -1) {
                throw new Exception("Target class column not found in filtered data");
            }
            filteredData.setClassIndex(targetIndex);
        }

        logger.info("Filtered dataset attributes: ");
        for (int i = 0; i < filteredData.numAttributes(); i++) {
            logger.info("Attribute {}: {}", i + 1, filteredData.attribute(i).name());
        }
        if(prediction == 1) {
            filteredData.setClassIndex(-1);
        }
        return filteredData;
    }

    public String csvToArff(InputStream inputStream, String fileReference) {
        File tempInputFile = null;
        File tempOutputFile = null;
        try {
            // Create a temporary file for the input data
            tempInputFile = File.createTempFile("input", getFileExtension(fileReference));
            Files.copy(inputStream, tempInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Determine if the file is CSV or ARFF and load the data accordingly
            Instances data;
            if (getFileExtension(fileReference).equalsIgnoreCase(".arff")) {
                ArffLoader arffLoader = new ArffLoader();
                arffLoader.setSource(tempInputFile);
                data = arffLoader.getDataSet();
            } else {
                CSVLoader csvLoader = new CSVLoader();
                csvLoader.setSource(tempInputFile);
                data = csvLoader.getDataSet();
            }
            // Create a separate temporary file for the ARFF output
            tempOutputFile = File.createTempFile("output", ".arff");

            // Save the data in ARFF format to the output file
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(tempOutputFile);
            saver.writeBatch();

            // Return the path of the output file
            return tempOutputFile.getAbsolutePath();
        } catch (IOException e) {
            throw new FileProcessingException("Failed to convert file to ARFF format", e);
        } finally {
            // Clean up the temporary input file
            if (tempInputFile != null) {
                tempInputFile.delete();
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndex);
    }

    public Instances loadPredictionDataset(DatasetConfiguration datasetConfiguration) throws Exception {
        URI datasetUri = new URI(datasetConfiguration.getDataset().getFileUrl());
        logger.info("Dataset URI: {}", datasetUri);
        String bucketName = Paths.get(datasetUri.getPath()).getName(0).toString();
        String objectName = Paths.get(datasetUri.getPath()).subpath(1, Paths.get(datasetUri.getPath()).getNameCount()).toString();
        logger.info("Bucket Name: {}", bucketName);
        logger.info("Object Name: {}", objectName);

        InputStream datasetStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        logger.info("Dataset Stream obtained successfully.");

        // Convert the dataset to ARFF format if it is in CSV or Excel format
        String fileExtension = getFileExtension(objectName);
        if (fileExtension.equalsIgnoreCase(".csv")) {
            String arffFilePath = csvToArff(datasetStream, objectName);
            datasetStream = Files.newInputStream(Paths.get(arffFilePath));
        }

        Instances data = new ConverterUtils.DataSource(datasetStream).getDataSet();
        int prediction = 1;
        data = selectColumns(data, datasetConfiguration.getBasicAttributesColumns(), null, prediction); // No target column for prediction
        data.setClassIndex(-1);
        return data;
    }
}
