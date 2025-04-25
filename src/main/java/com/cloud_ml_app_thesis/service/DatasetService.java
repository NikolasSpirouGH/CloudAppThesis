package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.dataset.DatasetSelectTableDTO;
import com.cloud_ml_app_thesis.dto.request.dataset.DatasetCreateRequest;
import com.cloud_ml_app_thesis.dto.request.dataset.DatasetSearchRequest;
import com.cloud_ml_app_thesis.dto.request.dataset.DatasetUploadRequest;
import com.cloud_ml_app_thesis.dto.request.dataset_configuration.DatasetConfigurationCreateRequest;
import com.cloud_ml_app_thesis.dto.response.ApiResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.entity.DatasetConfiguration;
import com.cloud_ml_app_thesis.enumeration.status.TrainingStatusEnum;
import com.cloud_ml_app_thesis.exception.MinioFileUploadException;

import com.cloud_ml_app_thesis.repository.DatasetConfigurationRepository;
import com.cloud_ml_app_thesis.repository.dataset.DatasetRepository;
import com.cloud_ml_app_thesis.repository.TrainRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.specification.DatasetSpecification;
import com.cloud_ml_app_thesis.util.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.cloud_ml_app_thesis.exception.FileProcessingException;



@Service
@RequiredArgsConstructor
public class DatasetService {

    private final MinioService minioService;
    private final CategoryService categoryService;

    private final DatasetRepository datasetRepository;
    private final DatasetConfigurationRepository datasetConfigurationRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);


    private final MinioClient minioClient;
    private final ObjectMapper objectMapper;

    @Value("ml-datasets")
    private String bucketName;

    @Value("http://127.0.0.1:9000")
    private String minioUrl;


/*
    public Integer uploadDataset(MultipartFile file, String email) {
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
            if(user.isPresent()) {
                dataset.setUser(user.get());
            } else {
                return null;
            }
            dataset.setUploadedDateTime(LocalDateTime.now());
            dataset.setFileName(file.getOriginalFilename());

            if(datasetRepository.findByFileUrl(fileUrl).isPresent()) {
                //TODO CUSTOM EXCEPTION AND LOGGING
                throw new IllegalArgumentException();
            }
            dataset = datasetRepository.save(dataset);
            return dataset.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
*/


    public Page<Dataset> searchDatasets(DatasetSearchRequest request, int page, int size, String sortBy, String sortDirection) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());
        String username = isAuthenticated ? authentication.getName() : null;

        boolean hasExplicitAccess = isAuthenticated && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_DATASET_MANAGER"));

        // Enforce validation rules before executing query
        if (!isAuthenticated) {
            // Unauthenticated users **MUST request only public datasets**
            if (request.getIsPublic() == null || !request.getIsPublic()) {
                throw new AuthorizationDeniedException("You can see only public datasets. For further access, please login or register.");
            }
        } else if (!hasExplicitAccess) {
            // Regular users **CANNOT search for private datasets of other users**
            if (request.getIsPublic() == null || !request.getIsPublic()) {
                if (request.getOwnerUsername() != null && !request.getOwnerUsername().equals(username)) {
                    throw new AuthorizationDeniedException("You cannot search for private datasets of other users.");
                }
                request.setOwnerUsername(username); // Restrict private dataset search to the authenticated user
            }
        }
        //  Fetch category IDs for deep search (if enabled)
        Set<Integer> categoryIds = request.getCategoryId() != null
                ? categoryService.getChildCategoryIds(request.getCategoryId(), request.getIncludeChildCategories() != null && request.getIncludeChildCategories())
                : null;

        Specification<Dataset> spec = DatasetSpecification.getDatasetsByCriteria(request, categoryIds);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        return datasetRepository.findAll(spec, pageable);
    }
    @Transactional
    //TODO check what is going on here.
    public ApiResponse<?> uploadDataset(DatasetUploadRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User could not be found!"));

        String originalFilename = file.getOriginalFilename();
        if(originalFilename.isBlank()){
            return new ApiResponse<>(null, "1", "Filename cannot be empty", new Metadata());
        }

        String uniqueFilename = FileUtil.generateUniqueFilename(originalFilename, user.getUsername());

        try {
            minioService.uploadFile(file, uniqueFilename);
        } catch (MinioFileUploadException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        Dataset dataset = new Dataset();
        dataset.setUser(user);
        dataset.setOriginalFileName(originalFilename);
        dataset.setFileName(uniqueFilename);
        dataset.setFilePath("dataset/" + uniqueFilename);
        dataset.setFileSize(file.getSize());
        dataset.setContentType(file.getContentType());
        dataset.setUploadDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));

        try {
           dataset = datasetRepository.save(dataset);

        } catch (DataAccessException e){
            logger.error("Failed to save the Dataset '{}' for user '{}'.", dataset.getOriginalFileName(), username );
            throw e;
        }

        return new ApiResponse<>(dataset.getId().toString(), null, null, new Metadata());
    }
  @Transactional
    public ApiResponse<?> uploadDataset(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User could not be found!"));

        String originalFilename = file.getOriginalFilename();
        if(originalFilename.isBlank()){
            return new ApiResponse<>(null, "1", "Filename cannot be empty", new Metadata());
        }

        String uniqueFilename = FileUtil.generateUniqueFilename(originalFilename, user.getUsername());

        try {
            minioService.uploadFile(file, uniqueFilename);
        } catch (MinioFileUploadException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        Dataset dataset = new Dataset();
        dataset.setUser(user);
        dataset.setOriginalFileName(originalFilename);
        dataset.setFileName(uniqueFilename);
        dataset.setFilePath("dataset/" + uniqueFilename);
        dataset.setFileSize(file.getSize());
        dataset.setContentType(file.getContentType());
        dataset.setUploadDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));

        try {
           dataset = datasetRepository.save(dataset);

        } catch (DataAccessException e){
            logger.error("Failed to save the Dataset '{}' for user '{}'.", dataset.getOriginalFileName(), username );
            throw e;
        }

        return new ApiResponse<>(dataset.getId().toString(), null, null, new Metadata());
    }
    @Transactional
    public ApiResponse<?> createDataset(DatasetCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User userRequested = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User requested the creation could not be found"));
        User ownerUser = userRepository.findByUsername(request.getOwnerUsername()).orElseThrow(() -> new EntityNotFoundException("User requested the creation could not be found"));

        MultipartFile file = request.getFile();

        String originalFilename = file.getOriginalFilename();
        if(originalFilename.isBlank()){
            return new ApiResponse<>(null, "1", "Filename cannot be empty", new Metadata());
        }

        String uniqueFilename = FileUtil.generateUniqueFilename(originalFilename,ownerUser.getUsername());

        Optional<Dataset> datasetTemp = datasetRepository.findByFileName();

        try {
            minioService.uploadFile(file, uniqueFilename);
        } catch (MinioFileUploadException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        Dataset dataset = new Dataset();
        dataset.setUser(ownerUser);
        dataset.setOriginalFileName(originalFilename);
        dataset.setFileName(uniqueFilename);
        dataset.setFilePath("dataset/" + uniqueFilename);
        dataset.setFileSize(file.getSize());
        dataset.setContentType(file.getContentType());
        dataset.setUploadDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));

        try {
           dataset = datasetRepository.save(dataset);

        } catch (DataAccessException e){
            logger.error("Failed to save the Dataset '{}' for user '{}'.", dataset.getOriginalFileName(), username );
            throw e;
        }

        return new ApiResponse<>(dataset.getId().toString(), null, null, new Metadata());
    }

    /*public MultipartFile getDatasetByTrainingId(Integer trainingId){
        Training training = trainRepository.findById(trainingId).orElseThrow(() -> new EntityNotFoundException("Training with id " + trainingId + " does not exist"));
        Dataset dataset = datasetRepository.findByTrainingId(trainingId).orElseThrow(() -> new EntityNotFoundException("Training with id " + trainingId + " does not exist"));
        minioService.getFileInputStream(dataset.getOriginalFileName(), dataset.getFilePath());
    }*/

    public Dataset uploadDataset(MultipartFile file, User user)  {

        String originalFilename = file.getOriginalFilename();
        if(originalFilename == null || originalFilename.isBlank()){
            return null;
        }

        String objectName = FileUtil.generateUniqueFilename(originalFilename, user.getUsername());
        //TODO
        try {
            minioService.uploadFile(file, objectName);
        } catch (MinioFileUploadException e) {
            throw e;
        } catch (RuntimeException e){
            throw e;
        }
        Dataset dataset = new Dataset();
        dataset.setUser(user);
        dataset.setOriginalFileName(originalFilename);
        dataset.setFileName(objectName);
        dataset.setFilePath("dataset/" + objectName);
        dataset.setFileSize(file.getSize());
        dataset.setContentType(file.getContentType());
        dataset.setUploadDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));

        try {
           return datasetRepository.save(dataset);

        } catch (DataAccessException e){
            logger.error("Failed to save Dataset '{}' for user '{}'.", dataset.getOriginalFileName(), user.getUsername() );
            throw e;
        }
    }


//*********************************************************************************************************************
    public ApiResponse<?> getDatasets(String username){
        Optional<List<Dataset>> datasetsOptional = datasetRepository.findAllByUserUsername(username);
        if(datasetsOptional.isPresent()){
            List<DatasetSelectTableDTO> datasetSelectTableDTOS = datasetsOptional.get().stream()
                    .map(this::convertToDTO)
                    .toList();
            return new ApiResponse<List<DatasetSelectTableDTO>>(datasetSelectTableDTOS, null, null, new Metadata());
        }
        return new ApiResponse<String>("Could not find datasets for user '" + username + "'.", null, null, new Metadata());
    }

    private DatasetSelectTableDTO convertToDTO(Dataset dataset){
        DatasetSelectTableDTO dto = objectMapper.convertValue(dataset, DatasetSelectTableDTO.class);

        long completeCount = trainRepository.countByDatasetConfigurationDatasetIdAndStatus(dataset.getId(), TrainingStatusEnum.COMPLETED);
        dto.setCompleteTrainingCount(completeCount);

        long failedCount = trainRepository.countByDatasetConfigurationDatasetIdAndStatus(dataset.getId(), TrainingStatusEnum.FAILED);
        dto.setFailedTrainingCount(failedCount);

        return dto;
    }
//*********************************************************************************************************************

    public ApiResponse<List<String>> getDatasetUrls(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            //TODO LOGGER AND EXCEPTION HANDLING
            System.out.println("User do not exists");
        }
        List<String> datasetUrls = user.map(u -> u.getDatasets().stream()
                        .map(Dataset::getFilePath)
                        .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
        return new ApiResponse<List<String>>(datasetUrls, null, null, new Metadata());
    }

    public DatasetConfiguration getDatasetConfiguration(Integer datasetConfId) throws Exception {
        return datasetConfigurationRepository.findById(datasetConfId)
                .orElseThrow(() -> new Exception("Dataset configuration ID " + datasetConfId + " not found."));
    }


    //TODO CHECK THE PATHS and more
    public Instances loadDatasetInstancesByDatasetConfigurationFromMinio(DatasetConfiguration datasetConfiguration) throws Exception {
        URI datasetUri = new URI(datasetConfiguration.getDataset().getFilePath());
        logger.info("Dataset URI: {}", datasetUri);
        String bucketName = Paths.get(datasetUri.getPath()).getName(0).toString();
        String objectName = Paths.get(datasetUri.getPath()).subpath(1, Paths.get(datasetUri.getPath()).getNameCount()).toString();
        logger.info("Bucket Name: {}", bucketName);
        logger.info("Object Name: {}", objectName);

        InputStream datasetStream =  minioClient.getObject(
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
        URI datasetUri = new URI(datasetConfiguration.getDataset().getFilePath());
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
