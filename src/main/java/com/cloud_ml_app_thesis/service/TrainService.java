package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.train.MyTrainingDTO;
import com.cloud_ml_app_thesis.entity.*;
import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import com.cloud_ml_app_thesis.enumeration.status.TrainingStatusEnum;
import com.cloud_ml_app_thesis.dto.request.training.*;
import com.cloud_ml_app_thesis.dto.response.*;
import com.cloud_ml_app_thesis.repository.*;

import com.cloud_ml_app_thesis.repository.dataset.DatasetRepository;
import com.cloud_ml_app_thesis.repository.status.TrainingStatusRepository;
import com.cloud_ml_app_thesis.util.AlgorithmUtil;
import com.cloud_ml_app_thesis.util.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainingRequestHelperService trainingRequestHelperService;
    private final TrainRepository trainRepository;

    private final UserRepository userRepository;

    private final AlgorithmConfigurationRepository algorithmConfigurationRepository;

    private final AlgorithmRepository algorithmRepository;

    private final TrainingStatusRepository trainingStatusRepository;

    private final DatasetRepository datasetRepository;

    private final DatasetConfigurationRepository datasetConfigurationRepository;

    private final ModelService modelService;

    private final DatasetService datasetService;

    private final AlgorithmService algorithmService;

    private final ObjectMapper objectMapper;


    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);

    private AlgorithmConfiguration loadAlgorithmConfiguration(String algorithmConfigurationId, String algorithmId, String options){
        if(ValidationUtil.stringExists(algorithmConfigurationId)){
            Optional<AlgorithmConfiguration> algorithmConfigurationOptional = algorithmConfigurationRepository.findById(Integer.parseInt(algorithmConfigurationId));
            if(algorithmConfigurationOptional.isPresent()){
                return algorithmConfigurationOptional.get();
            } else{
                //TODO Throw Exception
                return null;
            }
        } else if(ValidationUtil.stringExists(algorithmId)){
           Optional<Algorithm> algorithmOptional = algorithmRepository.findById(Integer.parseInt(algorithmId));


        }
        return null;
    }


    //TODO Check if the Status is being correctly set at the correct time
    public MyResponse<?> startTraining(TrainingStartRequest request, User user) throws Exception {


        TrainingDataInput trainingDataInput =  trainingRequestHelperService.configureTrainingDataInputByTrainCase(request, user);

        train(trainingDataInput.getTraining(),trainingDataInput.getDataset(), trainingDataInput.getFilename(),trainingDataInput.getDatasetConfiguration(), trainingDataInput.getAlgorithmConfiguration() );

        return new MyResponse("Your model with id '"+trainingDataInput.getTraining().getId() +"' is being training!",null, null,new Metadata());

    }

    @Async
    public CompletableFuture<Void> train(Training training, Instances data, String filename, DatasetConfiguration datasetConfiguration, AlgorithmConfiguration algorithmConfiguration) {
        return CompletableFuture.runAsync(() -> {

            try {

//                Instances data = DatasetUtil.prepareDataset(dataset, filename, datasetConfiguration);
                //data = datasetService.selectColumns(data, datasetConfiguration.getBasicAttributesColumns(), datasetConfiguration.getTargetColumn());
                String algorithmClassName = algorithmConfiguration.getAlgorithm().getClassName();
                boolean isClassifier = AlgorithmUtil.isClassifier(algorithmClassName);
                boolean isClusterer = AlgorithmUtil.isClusterer(algorithmClassName);


                //TODO see the save for finished date to not be that many saves
                if (isClassifier) {
                    com.cloud_ml_app_thesis.entity.status.TrainingStatus statusRunning = trainingStatusRepository.findByName(TrainingStatusEnum.RUNNING)
                            .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
                    training.setStatus(statusRunning);
                    trainRepository.save(training);

                    Classifier cls = trainClassifier(training, algorithmClassName, algorithmConfiguration, data);

                    com.cloud_ml_app_thesis.entity.status.TrainingStatus statusComplete = trainingStatusRepository.findByName(TrainingStatusEnum.COMPLETED)
                            .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));

                    training.setStatus(statusComplete);
                    training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
                    trainRepository.save(training);

                    evaluateAndSaveClassifier(training, cls, data);
                } else if (isClusterer) {
                    com.cloud_ml_app_thesis.entity.status.TrainingStatus statusRunning = trainingStatusRepository.findByName(TrainingStatusEnum.RUNNING)
                            .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
                    training.setStatus(statusRunning);
                    trainRepository.save(training);
                    Clusterer clus = trainClusterer(training, algorithmClassName, algorithmConfiguration, data);
                    com.cloud_ml_app_thesis.entity.status.TrainingStatus statusCompleted = trainingStatusRepository.findByName(TrainingStatusEnum.COMPLETED)
                            .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
                    training.setStatus(statusCompleted);
                    training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
                    trainRepository.save(training);
                    evaluateAndSaveClusterer(training, clus, data);
                } else {
                    logger.error("Unsupported algorithm type for algorithmClassName: {}", algorithmClassName);
                    Clusterer clus = trainClusterer(training, algorithmClassName, algorithmConfiguration, data);
                    com.cloud_ml_app_thesis.entity.status.TrainingStatus statusComplete = trainingStatusRepository.findByName(TrainingStatusEnum.COMPLETED)
                            .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
                    training.setStatus(statusComplete);
                    training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
                    trainRepository.save(training);
                }

            } catch (MinioException e) {
                handleTrainingFailure(training, e.getMessage());
            } catch (Exception e) {
                handleTrainingFailure(training, e);
            }
        });
    }

    @NotNull
    private static DatasetConfiguration getDatasetConfiguration(TrainingStartRequest request, Dataset dataset) {
        DatasetConfiguration datasetConfiguration = new DatasetConfiguration();
        if(request.getBasicCharacteristicsColumns() != null){
            if(!request.getBasicCharacteristicsColumns().isBlank()){
                datasetConfiguration.setBasicAttributesColumns(request.getBasicCharacteristicsColumns());
            }
        }
        if(request.getTargetClassColumn() != null){
            if(!request.getTargetClassColumn().isBlank()){
                datasetConfiguration.setTargetColumn(request.getTargetClassColumn());
            }
        }

        datasetConfiguration.setDataset(dataset);
        return datasetConfiguration;
    }


    private Training initializeTraining(Integer trainingId) {
        Optional<Training> trainingOpt = trainRepository.findById(trainingId);
        if (trainingOpt.isEmpty()) {
            logger.error("Training ID {} not found.", trainingId);
            return null;
        }
        Training training = trainingOpt.get();
        com.cloud_ml_app_thesis.entity.status.TrainingStatus statusRunning = trainingStatusRepository.findByName(TrainingStatusEnum.RUNNING)
                .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
        training.setStatus(statusRunning);
        training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
        Training t = trainRepository.save(training);
        return t;
    }

    private boolean validateDatasetConfiguration(Training training, DatasetConfiguration datasetConfiguration) {
        if (datasetConfiguration.getDataset() == null) {
            logger.error("Dataset is null for Dataset Configuration ID: {}", datasetConfiguration.getId());
            com.cloud_ml_app_thesis.entity.status.TrainingStatus statusFailed = trainingStatusRepository.findByName(TrainingStatusEnum.FAILED)
                    .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
            training.setStatus(statusFailed);
            training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
            trainRepository.save(training);            trainRepository.save(training);
            return false;
        }
        logger.info("Dataset File URL: {}", datasetConfiguration.getDataset().getFilePath());
        training.setDatasetConfiguration(datasetConfiguration);
        return true;
    }

    private Instances prepareData(DatasetConfiguration datasetConfiguration) throws Exception {
        return datasetService.loadDatasetInstancesByDatasetConfigurationFromMinio(datasetConfiguration);
    }

    private Classifier trainClassifier(Training training, String algorithmClassName, AlgorithmConfiguration algorithmConfiguration, Instances data) throws Exception {
        data.randomize(new Random(1));
        int trainSize = (int) Math.round(data.numInstances() * 0.6);
        Instances train = new Instances(data, 0, trainSize);

        Classifier cls = AlgorithmUtil.getClassifierInstance(algorithmClassName);

        String[] optionsArray = algorithmConfiguration.getOptions().split(",");
        //TODO exception ??
        AlgorithmUtil.setClassifierOptions(cls, optionsArray);

        cls.buildClassifier(train);
        return cls;
    }

    private Clusterer trainClusterer(Training training, String algorithmClassName, AlgorithmConfiguration algorithmConfiguration, Instances data) throws Exception {
        Clusterer clus = AlgorithmUtil.getClustererInstance(algorithmClassName);
        String[] optionsArray = algorithmConfiguration.getOptions().split(",");
        //TODO exception ??
        AlgorithmUtil.setClustererOptions(clus, optionsArray);

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

            com.cloud_ml_app_thesis.entity.status.TrainingStatus statusFailed = trainingStatusRepository.findByName(TrainingStatusEnum.FAILED)
                    .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
            training.setStatus(statusFailed);
            training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
            trainRepository.save(training);
        }
    }

    private void handleTrainingFailure(Training training, Exception exception) {
        logger.error("Training failed: {}", exception.getMessage(), exception);
        if (training != null) {
            com.cloud_ml_app_thesis.entity.status.TrainingStatus statusFailed = trainingStatusRepository.findByName(TrainingStatusEnum.FAILED)
                    .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
            training.setStatus(statusFailed);
            training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));

            trainRepository.save(training);
        }
    }

    public Training checkTraining(Integer trainingId) {
        return trainRepository.findById(trainingId).orElse(null);
    }

    public MyResponse<?> getTrainings() {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByOrderByStatusAsc();
            if(trainings.isEmpty()){
                return new MyResponse<>(null, "Failed to retrieve the Trainings.",null,new Metadata());
            }
            return new MyResponse<>(null, null, null, null);
        } catch (DataAccessException e){
            logger.error("Failed to retrieve the Trainings.",e);
            return new MyResponse<>(null, null, null, null);
        }
    }
    public MyResponse<?> getTrainings(String username, TrainingStatusEnum status) {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByUserUsernameAndStatus(username, status);
            if(trainings == null || trainings.isEmpty()){
                return new MyResponse("Trainings for user '" + username + "' could not be found.", null, null, new Metadata());
            }
            return new MyResponse(trainings, null, "Trainings found.", new Metadata());
        } catch (DataAccessException e){
            logger.error("Failed to retrieve Trainings of user '"+username+"'.",e);
            return new MyResponse(null,"An error occurred while tried to retrieve the Trainings of user '"+username+"'.",null,new Metadata());
        }
    }

    public MyResponse<?> getTrainings(String username) {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByUserUsernameOrderByFinishedDateDesc(username);
            if(trainings.isPresent()){
                List<MyTrainingDTO> myTrainingDTO = trainings.get()
                        .stream()
                        .map(this::convertToMyTrainingDTO)
                        .toList();
                return new MyResponse<>(myTrainingDTO, null, null, new Metadata());
            }
            return new MyResponse<>("Trainings for user '" + username + "' could not be found.", null, null, new Metadata());

        } catch (DataAccessException e){
            logger.error("Failed to retrieve Trainings of user '"+username+"'.",e);
            return new MyResponse(null, null, "An error occurred while tried to retrieve the Trainings of user '"+username+"'.",  new Metadata());
        }
    }
    public MyResponse<?> getTraining(int id){
      try {
          Optional<Training> trainingOptional = trainRepository.findById(id);
          if(trainingOptional.isPresent()){
              return new MyResponse(trainingOptional.get(), null, "Training found.", new Metadata());
          }
          return new MyResponse<>(null, null, "Training with id '" + id + "' could not be found.", new Metadata());
      } catch (DataAccessException e){
          logger.error("Failed to retrieve Training with id '"+id+"'.",e);
          return new MyResponse("An error occurred while tried to retrieve data for training with id '"+id+"'.", null, "Failed to retrieve Training with id '", new Metadata());
      }
    }

    private MyTrainingDTO convertToMyTrainingDTO(Training training){
        MyTrainingDTO dto = objectMapper.convertValue(training, MyTrainingDTO.class);

        dto.setBasicAttributesColumns(training.getDatasetConfiguration().getBasicAttributesColumns());
        dto.setTargetClassColumn(training.getDatasetConfiguration().getTargetColumn());

        dto.setAlgorithmOptions(training.getAlgorithmConfiguration().getOptions());
        dto.setModelId(training.getModel().getId());

        return dto;
  }

}
