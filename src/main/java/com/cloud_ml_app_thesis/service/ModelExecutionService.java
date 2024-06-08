package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.DatasetConfiguration;
import com.cloud_ml_app_thesis.entity.Model;
import com.cloud_ml_app_thesis.entity.ModelExecution;
import com.cloud_ml_app_thesis.repository.DatasetConfigurationRepository;
import com.cloud_ml_app_thesis.repository.ModelExecutionRepository;
import com.cloud_ml_app_thesis.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
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
    private final DatasetConfigurationRepository datasetConfigurationRepository;
    private final DatasetService datasetService;

    private static final Logger logger = LoggerFactory.getLogger(ModelExecutionService.class);

    public ModelExecution executeModel(Integer modelId, Integer datasetId) throws Exception {
        // Perform prediction
        List<Double> predictions = predict(modelId, datasetId);
        ModelExecution execution = new ModelExecution();
        execution.setModel(modelRepository.findById(modelId).get());
        execution.setExecutedAt(LocalDateTime.now());
        execution.setDatasetConfiguration(datasetConfigurationRepository.findById(datasetId).get());
        execution.setPredictionResult(predictions.toString());
        execution.setSuccess(true);

        return modelExecutionRepository.save(execution);
    }

    public List<Double> predict(Integer modelId, Integer datasetId) throws Exception {
        Object model = modelService.loadModel(modelId);
        DatasetConfiguration datasetConfiguration = datasetConfigurationRepository.findById(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset configuration not found with id: " + datasetId));
        Instances dataset = datasetService.loadDataset(datasetConfiguration);

        if (model instanceof Classifier) {
            return predictWithClassifier((Classifier) model, dataset);
        } else if (model instanceof Clusterer) {
            return predictWithClusterer((Clusterer) model, dataset);
        } else {
            throw new RuntimeException("Unsupported model type");
        }
    }

    private List<Double> predictWithClassifier(Classifier classifier, Instances dataset) throws Exception {
        logger.info("Number of instances in the dataset: {}", dataset.numInstances());
        List<Double> predictions = new ArrayList<>();
        for (int i = 0; i < dataset.numInstances(); i++) {
            try {
                double prediction = classifier.classifyInstance(dataset.instance(i));
                predictions.add(prediction);
                logger.info("Instance {}: Predicted value: {}", i, prediction);
            } catch (Exception e) {
                logger.error("Error predicting instance {}: {}", i, e.getMessage(), e);
                throw e; // Re-throw the exception after logging it
            }
        }
        logger.info("Predictions completed successfully. Total predictions: {}", predictions.size());
        return predictions;
    }

    private List<Double> predictWithClusterer(Clusterer clusterer, Instances dataset) throws Exception {
        List<Double> predictions = new ArrayList<>();
        for (int i = 0; i < dataset.numInstances(); i++) {
            try {
                int cluster = clusterer.clusterInstance(dataset.instance(i));
                predictions.add((double) cluster);
                logger.info("Instance {}: Predicted value: {}", i, predictions);
            } catch (Exception e) {
                logger.error("Error predicting instance {}: {}", i, e.getMessage(), e);
                throw e; // Re-throw the exception after logging it
            }
        }
        logger.info("Predictions completed successfully. Total predictions: {}", predictions.size());
        return predictions;
    }
}
