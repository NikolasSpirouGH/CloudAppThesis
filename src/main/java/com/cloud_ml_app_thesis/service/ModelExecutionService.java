package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.response.GenericResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import com.cloud_ml_app_thesis.entity.model.Model;
import com.cloud_ml_app_thesis.entity.model.ModelExecution;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.entity.status.ModelExecutionStatus;
import com.cloud_ml_app_thesis.enumeration.DatasetFunctionalTypeEnum;
import com.cloud_ml_app_thesis.enumeration.accessibility.ModelAccessibilityEnum;
import com.cloud_ml_app_thesis.enumeration.status.ModelExecutionStatusEnum;
import com.cloud_ml_app_thesis.helper.AuthorizationHelper;
import com.cloud_ml_app_thesis.repository.model.ModelExecutionRepository;
import com.cloud_ml_app_thesis.repository.model.ModelRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.repository.accessibility.ModelAccessibilityRepository;
import com.cloud_ml_app_thesis.repository.status.ModelExecutionStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelExecutionService {

    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final ModelService modelService;
    private final ModelExecutionRepository modelExecutionRepository;
    private final ModelAccessibilityRepository modelAccessibilityRepository;
    private final ModelExecutionStatusRepository modelExecutionStatusRepository;
    private final DatasetService datasetService;
    private final AuthorizationHelper authorizationHelper;

    private static final Logger logger = LoggerFactory.getLogger(ModelExecutionService.class);

    public ByteArrayResource executeModel(UserDetails userDetails, Integer modelId, MultipartFile predictDataset, String targetUsername){
        //TODO TO CHECK THE SECURITY AUTH LOGIC HERE.

        logger.info("Starting model execution for model ID: {} and dataset: {}", modelId, predictDataset.getOriginalFilename());

        User userRequested = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()-> new EntityNotFoundException("User requested not found"));


        Model modelEntity = modelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found with id: " + modelId));

        User targetUser = null;
        boolean isSuperUser = authorizationHelper.isSuperModelUser(userDetails);

        //1st we check if the action is from a superuser to execute the model for another user.
        //Retrieve the target user that is going to execute the model in real - ADMINS and MANAGERS can execute models for other users
        if(targetUsername != null && !targetUsername.isBlank()){
            targetUser = userRepository.findByUsername(targetUsername).orElseThrow(()-> new EntityNotFoundException("User requested not found"));
            if(!isSuperUser) {
                throw new AuthorizationDeniedException("You are not authorized to execute a model for another user");
            }
        } else {
            targetUsername = userRequested.getUsername();
            targetUser = userRequested;
        }

        ModelAccessibilityEnum accessibilityEnum = modelEntity.getAccessibility().getName();

        //2nd check if the Model is private, so Only the owner or a superuser can execute it
        // in case of not owner AND superuser we return unauthorized, otherwise continue
        if(accessibilityEnum == ModelAccessibilityEnum.PRIVATE){
            if(!isOwner(targetUsername, modelEntity.getTraining().getUser().getUsername()) && !isSuperUser){
                throw new AuthorizationDeniedException("You are not authorized to execute a model for another user");
            }
        }

        ModelExecution modelExecution = new ModelExecution();

        ModelExecutionStatus inProgressStatus = modelExecutionStatusRepository.findByName(ModelExecutionStatusEnum.IN_PROGRESS)
                        .orElseThrow(() -> new EntityNotFoundException("IN PROGRESS status not found"));
        modelExecution.setStatus(inProgressStatus);

        modelExecution = modelExecutionRepository.save(modelExecution);

        Instances predictDatasetInstances = null;
        try {
            predictDatasetInstances = datasetService.wekaFileToInstances(predictDataset);
        } catch (Exception e) {
            saveModelOnFailure(modelExecution);
            throw new RuntimeException(e);
        }

        List<String> predictions = null;
        try {
            predictions = predict(modelEntity.getMinioUrl(), predictDatasetInstances);
        } catch (Exception e) {
            saveModelOnFailure(modelExecution);
            throw new RuntimeException(e);
        }

        GenericResponse<Dataset> response = datasetService.uploadDataset(predictDataset, userRequested, DatasetFunctionalTypeEnum.PREDICT);

        Dataset dataset = response.getDataHeader();

        // Save execution in DB
        modelExecution.setModel(modelRepository.findById(modelId).orElseThrow());
        modelExecution.setExecutedAt(LocalDateTime.now());
        modelExecution.setPredictionResult(predictions.toString());
        modelExecution.setDataset(dataset);

        ByteArrayResource modelPredictions = null;
        try {
            modelPredictions = new ByteArrayResource(replaceQuestionMarksWithPredictionResults(predictDatasetInstances, predictions));
        } catch (Exception e) {
            saveModelOnFailure(modelExecution);
            throw new RuntimeException(e);
        }

        ModelExecutionStatus statusFinished = modelExecutionStatusRepository.findByName(ModelExecutionStatusEnum.FINISHED)
                        .orElseThrow(() -> new EntityNotFoundException("Model executions FINISHED status could not be found"));
        modelExecution.setStatus(statusFinished);
        modelExecutionRepository.save(modelExecution);

        return modelPredictions;
    }

    public ByteArrayResource getPredictionResults(Integer modelExecutionId) {
        ModelExecution execution = modelExecutionRepository
                .findById(modelExecutionId).orElseThrow(()-> new EntityNotFoundException("Model execution " + modelExecutionId + " not found"));

         // Already saved during initial prediction
        Instances predictInstances = null;
        try {
            predictInstances = datasetService.loadPredictionDataset(execution.getDataset());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] result = null;
        try {
            result = replaceQuestionMarksWithPredictionResults(predictInstances,  Arrays.asList(execution.getPredictionResult().split(",")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayResource(result);
    }

    public List<String> predict(String modelMinioUri, Instances predictDatasetInstances) throws Exception {
        Object model = modelService.loadModel(modelMinioUri);
        logger.info("Model loaded: {}", model.getClass().getName());

        if (model instanceof Classifier) {
            logger.info("Loading classifier...");
            return predictWithClassifier((Classifier) model, predictDatasetInstances);
        } else if (model instanceof Clusterer) {
            return predictWithClusterer((Clusterer) model, predictDatasetInstances);
        } else {
            logger.error("Unsupported model type: {}", model.getClass().getName());
            throw new RuntimeException("Unsupported model type");
        }
    }

    private List<String> predictWithClassifier(Classifier classifier, Instances dataset) throws Exception {
        dataset.setClassIndex(dataset.numAttributes() - 1);
        Attribute classAttribute = dataset.classAttribute(); // Get the class attribute
        logger.info("Performing prediction with classifier on dataset with {} instances", dataset.numInstances());
        List<String> predictions = new ArrayList<>();
        for (int i = 0; i < dataset.numInstances(); i++) {
            try {
                double prediction = classifier.classifyInstance(dataset.instance(i));
                String predictedLabel = classAttribute.value((int) prediction); // Get the class label
                logger.info("Instance {}: Predicted value: {}", i, predictedLabel);
                predictions.add(predictedLabel);
            } catch (Exception e) {
                logger.error("Error predicting instance {}: {}", i, e.getMessage(), e);
                throw e; // Re-throw the exception after logging it
            }
        }
        logger.info("Predictions completed successfully. Total predictions: {}", predictions.size());
        return predictions;
    }

    private List<String> predictWithClusterer(Clusterer clusterer, Instances dataset) throws Exception {
        logger.info("Performing prediction with clusterer on dataset with {} instances", dataset.numInstances());
        List<String> predictions = new ArrayList<>();
        for (int i = 0; i < dataset.numInstances(); i++) {
            try {
                int cluster = clusterer.clusterInstance(dataset.instance(i));
                String clusterLabel = "Cluster " + cluster; // Here you can map to more meaningful labels if available
                logger.info("Instance {}: Predicted cluster: {}", i, clusterLabel);
                predictions.add(clusterLabel);
            } catch (Exception e) {
                logger.error("Error predicting instance {}: {}", i, e.getMessage(), e);
                throw e; // Re-throw the exception after logging it
            }
        }
        logger.info("Cluster predictions completed successfully. Total predictions: {}", predictions.size());
        return predictions;
    }

    private byte[] replaceQuestionMarksWithPredictionResults(Instances predictInstances, List<String> predictions) throws Exception {
        predictInstances.setClassIndex(predictInstances.numAttributes() - 1);

        for (int i = 0; i < predictInstances.numInstances(); i++) {
            Instance instance = predictInstances.instance(i);
            instance.setClassValue(predictions.get(i));
        }
        // Convert Instances to ARFF format string
        String updatedArffString = predictInstances.toString();
        return updatedArffString.getBytes(StandardCharsets.UTF_8);
    }

    private void saveModelOnFailure(ModelExecution modelExecution){
        ModelExecutionStatus statusFailed = modelExecutionStatusRepository.findByName(ModelExecutionStatusEnum.FAILED)
                .orElseThrow(() -> new EntityNotFoundException("Model executions FINISHED status could not be found"));

        modelExecution.setStatus(statusFailed);
        modelExecutionRepository.save(modelExecution);
    }

    private boolean isOwner(String targetUsername, String ownerUsername){
        return targetUsername.equals(ownerUsername);
    }
}
