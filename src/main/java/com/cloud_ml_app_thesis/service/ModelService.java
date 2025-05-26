package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.config.BucketResolver;
import com.cloud_ml_app_thesis.dto.request.model.ModelFinalizeRequest;
import com.cloud_ml_app_thesis.entity.Category;
import com.cloud_ml_app_thesis.entity.Training;
import com.cloud_ml_app_thesis.entity.accessibility.ModelAccessibility;
import com.cloud_ml_app_thesis.entity.model.Keyword;
import com.cloud_ml_app_thesis.entity.model.Model;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.model.ModelShareHistory;
import com.cloud_ml_app_thesis.entity.status.ModelStatus;
import com.cloud_ml_app_thesis.enumeration.BucketTypeEnum;
import com.cloud_ml_app_thesis.enumeration.accessibility.ModelAccessibilityEnum;
import com.cloud_ml_app_thesis.enumeration.status.ModelStatusEnum;
import com.cloud_ml_app_thesis.repository.CategoryRepository;
import com.cloud_ml_app_thesis.repository.KeywordRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.repository.accessibility.ModelAccessibilityRepository;
import com.cloud_ml_app_thesis.repository.model.ModelRepository;
import com.cloud_ml_app_thesis.repository.TrainingRepository;
import com.cloud_ml_app_thesis.repository.status.ModelStatusRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.persistence.AccessType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instances;

import java.io.*;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final MinioClient minioClient;
    private final MinioService minioService;

    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ModelStatusRepository modelStatusRepository;
    private final ModelAccessibilityRepository accessibilityRepository;
    private final KeywordRepository keywordRepository;

    private final BucketResolver bucketResolver;
    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    private final TrainingRepository trainingRepository;

    @Value("${minio.url}")
    private String minioUrl;

    public String saveModelToMinio(String bucketName, String objectName, byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(
                                bais, data.length, -1)
                        .build());
        return minioUrl + "/" + bucketName + "/" + objectName;
    }

    public byte[] serializeModel(Object model) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(model);
            return bos.toByteArray();
        }
    }

    public Integer finalizeModel(Integer trainingId, UserDetails userDetails, ModelFinalizeRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User with username "+ userDetails.getUsername() +"not found"));

        // Step 1: Load training and check existence
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Training with ID " + trainingId + " not found"));

        // Step 2: Ownership check
        if (!training.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("You do not own this training.");
        }

        // Step 3: Check training status
        if (!training.getStatus().getName().toString().equalsIgnoreCase("COMPLETED")) {
            throw new IllegalStateException("Training must be completed before finalizing a model.");
        }

        // Step 4: Get existing model
        Model model = training.getModel();
        if (model == null) {
            throw new IllegalStateException("No model found for this training.");
        }

        // Step 5: Set model metadata
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setDataDescription(training.getDatasetConfiguration().getDataset().getDescription());
        model.setFinalizationDate(ZonedDateTime.now());
        model.setFinishedAt(training.getFinishedDate());

        // Set category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        model.setCategory(category);

        ModelAccessibilityEnum modelAccessibilityEnum =
                request.isPublic() ? ModelAccessibilityEnum.PUBLIC : ModelAccessibilityEnum.PRIVATE;

        // Step 6: Handle accessibility
        ModelAccessibility accessibility = accessibilityRepository.findByName(modelAccessibilityEnum)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access type: " + modelAccessibilityEnum));
        model.setAccessibility(accessibility);

        // Step 7: Set model status to FINALIZED
        ModelStatus finalizedStatus = modelStatusRepository.findByName(ModelStatusEnum.FINALIZED)
                .orElseThrow(() -> new IllegalStateException("Model status FINALIZED not configured"));
        model.setStatus(finalizedStatus);

        // Step 8: Handle keywords
        Set<Keyword> keywords = request.getKeywords().stream()
                .map(word -> keywordRepository.findByNameIgnoreCase(word)
                        .orElseGet(() -> keywordRepository.save(new Keyword(word))))
                .collect(Collectors.toSet());
        model.setKeywords(keywords);

        // Step 9: Save model
        modelRepository.save(model);


        return model.getId();
    }


    public void saveModel(Training training, String modelUrl, String results, String modelType, User user, boolean isFinalized) {
        Model model = new Model();
        model.setTraining(training);
        model.setMinioUrl(modelUrl); // Truncate if needed
        model.setEvaluation(results); // Truncate if needed
        model.setStatus(modelStatusRepository.findByName(ModelStatusEnum.TRAINING_COMPLETED).orElseThrow(() -> new EntityNotFoundException("Could not find FINISHED model status")));
        model.setModelType(modelType);
        model.setFinishedAt(training.getFinishedDate());
        model.setFinalized(isFinalized);
        modelRepository.save(model);
    }

    public String evaluateClassifier(Classifier cls, Instances train, Instances test) throws Exception {
        Evaluation eval = new Evaluation(train);
        eval.evaluateModel(cls, test);

        String results = "Classifier trained successfully. \nEvaluation results:\n" +
                "Accuracy: " + String.format("%.2f%%", eval.pctCorrect()) + "\n" +
                "Precision: " + String.format("%.2f%%", eval.weightedPrecision() * 100) + "\n" +
                "Recall: " + String.format("%.2f%%", eval.weightedRecall() * 100) + "\n" +
                "F1 Score: " + String.format("%.2f%%", eval.weightedFMeasure() * 100) + "\n" +
                "Summary: " + eval.toSummaryString();

        logger.info(results);
        return results;
    }

    public String evaluateClusterer(Clusterer clusterer, Instances data) throws Exception {
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
        eval.evaluateClusterer(data);

        String results = "Clusterer trained successfully. \nEvaluation results:\n" +
                eval.clusterResultsToString();

        logger.info(results);
        return results;
    }

    public Object loadModel(String modelMinioUri) throws Exception {
        logger.info("Loading model from minio URI: {}", modelMinioUri);

        URI modelUri = new URI(modelMinioUri);

        // Splitting the path to get bucket and object names
        String[] pathParts = modelUri.getPath().split("/");
        logger.info("Path parts: {}", (Object) pathParts);

        if (pathParts.length < 2) {
            throw new RuntimeException("Invalid model URI: " + modelUri);
        }

        String minioUrl = pathParts[0];
        String bucketName = pathParts[1];

        String objectName = String.join("/", Arrays.copyOfRange(pathParts, 2, pathParts.length));


        try {

            Object model = minioService.loadObject(bucketResolver.resolve(BucketTypeEnum.MODEL), objectName);
            logger.info("Model loaded successfully");

            return model;
        } catch (Exception e) {
            String errorMessage = "Unexpected error while loading model: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


}

