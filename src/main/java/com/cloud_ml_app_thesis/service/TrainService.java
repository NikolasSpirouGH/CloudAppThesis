package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.entity.AlgorithmConfiguration;
import com.cloud_ml_app_thesis.entity.DatasetConfiguration;
import com.cloud_ml_app_thesis.entity.Training;
import com.cloud_ml_app_thesis.enumeration.TrainingStatus;
import com.cloud_ml_app_thesis.repository.*;

import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;

    private final ModelService modelService;

    private final DatasetService datasetService;

    private final AlgorithmService algorithmService;

    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);

    public Integer createTrainingRequest(Integer algoId, Integer datasetConfId) {
        Training training = new Training();
        training.setStatus(TrainingStatus.REQUESTED);
        training.setStartedAt(LocalDateTime.now());
        trainRepository.save(training);
        train(algoId, datasetConfId, training.getId());
        return training.getId();
    }

    @Async
    public CompletableFuture<Void> train(Integer algoConfId, Integer datasetConfId, Integer trainingId) {
        return CompletableFuture.runAsync(() -> {
            Training training = null;
            try {
                training = initializeTraining(trainingId);
                if (training == null) return;

                DatasetConfiguration datasetConfiguration = datasetService.getDatasetConfiguration(datasetConfId);
                if (!validateDatasetConfiguration(training, datasetConfiguration)) return;

                AlgorithmConfiguration algorithmConfiguration = algorithmService.getAlgorithmConfiguration(algoConfId);
                Algorithm algorithm = algorithmConfiguration.getAlgorithm();
                logger.info("Algorithm Name: {}", algorithm.getName());
                training.setAlgorithmConfiguration(algorithmConfiguration);

                Instances data = prepareData(datasetConfiguration);
                //data = datasetService.selectColumns(data, datasetConfiguration.getBasicAttributesColumns(), datasetConfiguration.getTargetColumn());

                boolean isClassifier = algorithmService.isClassifier(algorithm);
                boolean isClusterer = algorithmService.isClusterer(algorithm);

                if (isClassifier) {
                    logger.info("Classifier Algorithm Name: {}", algorithm.getClassName());
                    Classifier cls = trainClassifier(training, algorithm, algorithmConfiguration, data);
                    training.setStatus(TrainingStatus.COMPLETE);
                    trainRepository.save(training);
                    evaluateAndSaveClassifier(training, cls, data);
                } else if (isClusterer) {
                    logger.info("Cluster Algorithm Name: {}", algorithm.getClassName());
                    Clusterer clus = trainClusterer(training, algorithm, algorithmConfiguration, data);
                    training.setStatus(TrainingStatus.COMPLETE);
                    trainRepository.save(training);
                    evaluateAndSaveClusterer(training, clus, data);
                } else {
                    logger.error("Unsupported algorithm type: {}", algorithm.getName());
                    training.setStatus(TrainingStatus.FAILED);
                    trainRepository.save(training);
                }
            } catch (MinioException e) {
                handleTrainingFailure(training, e.getMessage());
            } catch (Exception e) {
                handleTrainingFailure(training, e);
            }
        });
    }

    private Training initializeTraining(Integer trainingId) {
        Optional<Training> trainingOpt = trainRepository.findById(trainingId);
        if (trainingOpt.isEmpty()) {
            logger.error("Training ID {} not found.", trainingId);
            return null;
        }
        Training training = trainingOpt.get();
        training.setStatus(TrainingStatus.RUNNING);
        trainRepository.save(training);
        return training;
    }

    private boolean validateDatasetConfiguration(Training training, DatasetConfiguration datasetConfiguration) {
        if (datasetConfiguration.getDataset() == null) {
            logger.error("Dataset is null for Dataset Configuration ID: {}", datasetConfiguration.getId());
            training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);
            return false;
        }
        logger.info("Dataset File URL: {}", datasetConfiguration.getDataset().getFileUrl());
        training.setDatasetConfiguration(datasetConfiguration);
        return true;
    }

    private Instances prepareData(DatasetConfiguration datasetConfiguration) throws Exception {
        return datasetService.loadDataset(datasetConfiguration);
    }

    private Classifier trainClassifier(Training training, Algorithm algorithm, AlgorithmConfiguration algorithmConfiguration, Instances data) throws Exception {
        data.randomize(new Random(1));
        int trainSize = (int) Math.round(data.numInstances() * 0.6);
        Instances train = new Instances(data, 0, trainSize);

        Classifier cls = algorithmService.getClassifierInstance(algorithm);
        logger.info("Users options: {}", algorithmConfiguration.getOptions());
        logger.info("Defaults options: {}", algorithmConfiguration.getAlgorithm().getDefaultOptions());
        String[] optionsArray = algorithmService.convertToWekaOptions(algorithmConfiguration.getOptions(), algorithm.getDefaultOptions().replace(",", ""));
        logger.info("Merge options: {}", Arrays.toString(optionsArray));
        algorithmService.setClassifierOptions(cls, optionsArray);

        cls.buildClassifier(train);
        return cls;
    }

    private Clusterer trainClusterer(Training training, Algorithm algorithm, AlgorithmConfiguration algorithmConfiguration, Instances data) throws Exception {
        Clusterer clus = algorithmService.getClustererInstance(algorithm);
        String[] optionsArray = algorithmService.convertToWekaOptions(algorithmConfiguration.getOptions(), algorithmConfiguration.getAlgorithm().getDefaultOptions());
        logger.info("Options: {}", Arrays.toString(optionsArray));
        algorithmService.setClustererOptions(clus, optionsArray);

        clus.buildClusterer(data);
        return clus;
    }

    private void evaluateAndSaveClassifier(Training training, Classifier cls, Instances data) throws Exception {
        data.randomize(new Random(1));
        int trainSize = (int) Math.round(data.numInstances() * 0.6);
        int testSize = data.numInstances() - trainSize;
        Instances test = new Instances(data, trainSize, testSize);

        test.setClassIndex(test.numAttributes() - 1);
        String results = modelService.evaluateClassifier(cls, test, test);
        byte[] modelData = modelService.serializeModel(cls);

        String modelUrl = modelService.saveModelToMinio("ml-models", "model-" + training.getId(), modelData);
        String modelType = "classifier";
        modelService.saveModel(training.getId(), modelUrl, results, modelType);

        training.setResults(results);
        trainRepository.save(training);
    }

    private void evaluateAndSaveClusterer(Training training, Clusterer clus, Instances data) throws Exception {
        String results = modelService.evaluateClusterer(clus, data);
        byte[] modelData = modelService.serializeModel(clus);

        String modelUrl = modelService.saveModelToMinio("ml-models", "model-" + training.getId(), modelData);
        String modelType = "clusterer";
        modelService.saveModel(training.getId(), modelUrl, results, modelType);

        training.setResults(results);
        trainRepository.save(training);
    }

    private void handleTrainingFailure(Training training, String errorMessage) {
        logger.error("Training failed: {}", errorMessage);
        if (training != null) {
            training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);
        }
    }

    private void handleTrainingFailure(Training training, Exception exception) {
        logger.error("Training failed: {}", exception.getMessage(), exception);
        if (training != null) {
            training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);
        }
    }

    public Training checkTraining(Integer trainingId) {
        return trainRepository.findById(trainingId).orElse(null);
    }

    public List<Training> getTrainings() {
        return trainRepository.findAllByOrderByStatusAsc();

    }
}
