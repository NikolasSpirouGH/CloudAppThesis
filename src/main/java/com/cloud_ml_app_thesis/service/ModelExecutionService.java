package com.cloud_ml_app_thesis.service;


import com.cloud_ml_app_thesis.entity.ModelExecution;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.repository.DatasetConfigurationRepository;
import com.cloud_ml_app_thesis.repository.ModelExecutionRepository;
import com.cloud_ml_app_thesis.repository.ModelRepository;
import com.cloud_ml_app_thesis.repository.accessibility.ModelAccessibilityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instances;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModelExecutionService {

    private final ModelRepository modelRepository;
    private final ModelService modelService;
    private final ModelExecutionRepository modelExecutionRepository;
    private final ModelAccessibilityRepository modelAccessibilityRepository;
    private final DatasetService datasetService;

    private static final Logger logger = LoggerFactory.getLogger(ModelExecutionService.class);

    public ModelExecution executeModel(Integer modelId, MultipartFile predictDataset) throws Exception {
        logger.info("Starting model execution for model ID: {} and dataset ID: {}", modelId, predictDataset);

        List<String> predictions = predict(modelId, predictDataset);
        ModelExecution execution = new ModelExecution();
        execution.setModel(modelRepository.findById(modelId).get());
        execution.setExecutedAt(LocalDateTime.now());
        execution.setPredictionResult(predictions.toString());
        execution.setSuccess(true);

        logger.info("Model execution completed successfully with predictions: {}", predictions);
        return modelExecutionRepository.save(execution);
    }


    public List<String> predict(Integer modelId, MultipartFile dataset) throws Exception {
        Object model = modelService.loadModel(modelId);
        logger.info("Model loaded: {}", model.getClass().getName());
        Dataset uploadDataset = (Dataset)datasetService.uploadDataset(dataset).getDataHeader();
        Instances predictDataset = datasetService.wekaFileToInstances(dataset);

        logger.info("Loaded dataset with {} instances for prediction", predictDataset);
        logger.info("Dataset structure: {}", dataset);

        if (model instanceof Classifier) {
            logger.info("Loading classifier...");
            return predictWithClassifier((Classifier) model, predictDataset);
        } else if (model instanceof Clusterer) {
            return predictWithClusterer((Clusterer) model, predictDataset);
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


}
