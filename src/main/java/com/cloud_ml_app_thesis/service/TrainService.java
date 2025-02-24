package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.train.MyTrainingDTO;
import com.cloud_ml_app_thesis.entity.*;
import com.cloud_ml_app_thesis.enumeration.status.TrainingStatusEnum;
import com.cloud_ml_app_thesis.payload.request.TrainingRequest;
import com.cloud_ml_app_thesis.payload.response.*;
import com.cloud_ml_app_thesis.repository.*;

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

    /*//TODO Check if the Status is being correctly set at the correct time
    public CustomResponse startTraining(MultipartFile file, TrainingRequest request) {

        Training training = new Training();
        com.cloud_ml_app_thesis.entity.status.TrainingStatus statusRequested = trainingStatusRepository.findByName(TrainingStatus.REQUESTED)
                .orElseThrow(() ->  new EntityNotFoundException("Training status could not be found. Please try later."));
        training.setStatus(statusRequested);

        String algorithmId = request.getAlgorithmId();

        Optional<Algorithm> algorithmOptional = algorithmRepository.findById(Integer.parseInt(algorithmId));
        AlgorithmConfiguration algorithmConfiguration = new AlgorithmConfiguration();
        if(algorithmOptional.isPresent()){
            algorithmConfiguration.setAlgorithm(algorithmOptional.get());
        } else {
           *//* training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);*//*
            throw new AlgorithmNotFoundException("Algorithm could not be found");
        }

        Optional<User> user = userRepository.findByUsername("nickriz");
        if(user.isEmpty()){
           *//* training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);*//*
            throw new UserNotFoundException("User could not be found");
        }
        //TODO Why we use replace() and we dont format it by the time we initialize the records in the Database?
        String formattedOptions = AlgorithmUtil.formatUserOptionsOptions(request.getAlgorithmOptions(), algorithmOptional.get().getDefaultOptions().replace(",", ""));
        algorithmConfiguration.setOptions(formattedOptions);
        algorithmConfiguration.setUser(user.get());
        try {
            algorithmConfiguration = algorithmConfigurationRepository.save(algorithmConfiguration);
        } catch (DataAccessException e){
            logger.error("Failed to save AlgorithmConfiguration", e);
            throw e;
        }
        training.setAlgorithmConfiguration(algorithmConfiguration);

        Dataset dataset = datasetService.uploadDataset(file, user.get());
        DatasetConfiguration datasetConfiguration = getDatasetConfiguration(request, dataset);
        try{
            datasetConfiguration = datasetConfigurationRepository.save(datasetConfiguration);
        } catch(DataAccessException e){
            logger.error("Failed to save DatasetConfiguration", e);
            throw e;
        }
        training.setDatasetConfiguration(datasetConfiguration);
        training.setStartedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
//            trainRepository.save(training);

        //TODO try catch for train in order to save training with correct status



            try {
                training = trainRepository.save(training);
            } catch(DataAccessException dae){
                logger.error("Failed to save DatasetConfiguration", dae);
                throw dae;
            }
            train(training, file, dataset.getFileName(), datasetConfiguration, algorithmOptional.get().getClassName(), algorithmConfiguration);
            return new DataMapResponse("Your model with id '"+training.getId() +"' is being training!",Collections.singletonMap("id", training.getId()));

    }*/

    //TODO Check if the Status is being correctly set at the correct time
    public CustomResponse startTraining(TrainingRequest request) throws Exception {


        TrainingDataInput trainingDataInput =  trainingRequestHelperService.configureTrainingDataInputByTrainCase(request);

        train(trainingDataInput.getTraining(),trainingDataInput.getDataset(), trainingDataInput.getFilename(),trainingDataInput.getDatasetConfiguration(), trainingDataInput.getAlgorithmConfiguration() );
        return new DataMapResponse("Your model with id '"+trainingDataInput.getTraining().getId() +"' is being training!",Collections.singletonMap("id", trainingDataInput.getTraining().getId()));


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
    private static DatasetConfiguration getDatasetConfiguration(TrainingRequest request, Dataset dataset) {
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

    /*  @Async
    public CompletableFuture<Void> train(Training training, Dataset dataset, DatasetConfiguration datasetConfiguration, String algorithm, AlgorithmConfiguration algorithmConfiguration) {
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

                boolean isClassifier = AlgorithmUtil.isClassifier(algorithm);
                boolean isClusterer = AlgorithmUtil.isClusterer(algorithm);

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
                training.setStartedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));

            } catch (MinioException e) {
                handleTrainingFailure(training, e.getMessage());
            } catch (Exception e) {
                handleTrainingFailure(training, e);
            }
        });
    }

  */  private Training initializeTraining(Integer trainingId) {
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

    public CustomResponse getTrainings() {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByOrderByStatusAsc();
            if(trainings.isEmpty()){
                return new InformationResponse("Failed to retrieve the Trainings.");


            }
            return new ObjectsDataResponse(trainings.get());
        } catch (DataAccessException e){
            logger.error("Failed to retrieve the Trainings.",e);
            return new ErrorResponse("An error occurred while tried to retrieve the Trainings.");
        }
    }
    public CustomResponse getTrainings(String username, TrainingStatusEnum status) {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByUserUsernameAndStatus(username, status);
            if(trainings == null || trainings.isEmpty()){
                return new InformationResponse("Trainings for user '" + username + "' could not be found.");
            }
            return new SingleObjectDataResponse(trainings, "Trainings found.");
        } catch (DataAccessException e){
            logger.error("Failed to retrieve Trainings of user '"+username+"'.",e);
            return new ErrorResponse("An error occurred while tried to retrieve the Trainings of user '"+username+"'.");
        }
    }

    public CustomResponse getTrainings(String username) {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByUserUsernameOrderByFinishedDateDesc(username);
            if(trainings.isPresent()){
                List<MyTrainingDTO> myTrainingDTO = trainings.get()
                        .stream()
                        .map(this::convertToMyTrainingDTO)
                        .toList();
                return new ObjectsDataResponse(myTrainingDTO);
            }
            return new InformationResponse("Trainings for user '" + username + "' could not be found.");

        } catch (DataAccessException e){
            logger.error("Failed to retrieve Trainings of user '"+username+"'.",e);
            return new ErrorResponse("An error occurred while tried to retrieve the Trainings of user '"+username+"'.");
        }
    }
    public CustomResponse getTraining(int id){
      try {
          Optional<Training> trainingOptional = trainRepository.findById(id);
          if(trainingOptional.isPresent()){
              return new SingleObjectDataResponse(trainingOptional.get(), "Training found.");
          }
          return new InformationResponse("Training with id '" + id + "' could not be found.");
      } catch (DataAccessException e){
          logger.error("Failed to retrieve Training with id '"+id+"'.",e);
          return new ErrorResponse("An error occurred while tried to retrieve data for training with id '"+id+"'.");
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



/*

  private Object findTrainCase(TrainingRequest request, boolean multipartFileExist, MultipartFile multipartFile, String username) throws Exception {

      String datasetId = request.getDatasetId();
      boolean datasetIdExist = ValidationUtil.stringExists(datasetId);

      String datasetConfigurationId = request.getDatasetConfigurationId();
      boolean datasetConfigurationIdExist = ValidationUtil.stringExists(datasetConfigurationId);

      String basicCharacteristicsColumns = request.getBasicCharacteristicsColumns();
      boolean basicCharacteristicsColumnsExist = ValidationUtil.stringExists(basicCharacteristicsColumns);

      String targetClassColumn = request.getTargetClassColumn();
      boolean targetClassColumnExist = ValidationUtil.stringExists(targetClassColumn);

      String algorithmId = request.getAlgorithmId();
      boolean algorithmIdExist = ValidationUtil.stringExists(algorithmId);

      String algorithmOptions = request.getAlgorithmOptions();
      boolean algorithmOptionsExist = ValidationUtil.stringExists(algorithmOptions);

      String algorithmConfigurationId = request.getAlgorithmConfigurationId();
      boolean algorithmConfigurationIdExist = ValidationUtil.stringExists(algorithmConfigurationId);

      String trainingId = request.getTrainingId();
      boolean trainingIdExist = ValidationUtil.stringExists(trainingId);

      String modelId = request.getModelId();
      boolean modelIdExist = ValidationUtil.stringExists(modelId);

      // 1st check - Can't provide trainingId and modelId at the same time
      if(trainingIdExist && modelIdExist){
          return new ErrorResponse("You can't train a model based on a Training and a Model at the same time.");
      }

      // 2nd check - Can't provide datasetId and a new File
      if(datasetIdExist && multipartFileExist){
          return new ErrorResponse("You can't train a model with an already uploaded Dataset and a new Dataset File at the same time.");
      }

      // 3rd check - Can't provide datasetId and datasetConfigurationId at the same time
      if(datasetIdExist && datasetConfigurationIdExist){
          return new ErrorResponse("You can't train a model providing a datasetId and a datasetConfigurationId at the same time.");
      }

      // 4th check - Can't provide datasetId and datasetConfigurationId at the same time
      if(datasetConfigurationIdExist && basicCharacteristicsColumnsExist && targetClassColumnExist){
          return new ErrorResponse("You can't train a model providing a datasetConfigurationId and booth basic characteristics columns and target class column at the same time.");
      }

      // 5th check - Can't provide algorithmId and algorithmConfigurationId at the same time
      if(algorithmIdExist && algorithmConfigurationIdExist){
          return new ErrorResponse("You can't train a model providing a algorithmId and a algorithmConfigurationId at the same time.");
      }
      // 6th check - Can't provide algorithmId and algorithmConfigurationId at the same time
      if(algorithmConfigurationIdExist && algorithmOptionsExist){
          return new ErrorResponse("You can't train a model providing algorithm options and a algorithmConfigurationId at the same time.");
      }

      // 7th check - If MultiPartFile is provided then upload the dataset and get the datasetId to continue
      if(multipartFileExist){
          CustomResponse uploadFileResponse = datasetService.uploadDataset(multipartFile, username);
          if(uploadFileResponse instanceof IdResponse){
              datasetId = ((IdResponse)uploadFileResponse).getId();
              datasetIdExist = true;
          } else if (uploadFileResponse instanceof ErrorResponse) {
              return (ErrorResponse) uploadFileResponse;
          } else{
              return new ErrorStatusResponse("Unexpected Error while trying to upload the File.", HttpStatus.INTERNAL_SERVER_ERROR);
          }
      }

      //*********** CONFIGURING TRAINING DATASET**************
       //A) DATASET config
        */
/* 1st Dataset CASE - datasetId-Or-MultipartFile AND OPTIONALLY at least one of the [basicCharacteristicsColumns, targetClassColumn]
            - if not provided, then last column is the target class and all the previous columns are basic characteristics
        *//*

      TrainingDataInput trainingDataInput = new TrainingDataInput();
      DatasetConfiguration datasetConfiguration = null;
      if(datasetIdExist){
          //first set the DatasetConfiguration
          //TODO change the Exception message
          datasetConfiguration = new DatasetConfiguration();
          Dataset dataset = datasetRepository.findById(Integer.parseInt(datasetId)).orElseThrow(() -> new EntityNotFoundException("The dataset for your training could not be found!"));
          datasetConfiguration.setDataset(dataset);
          if(basicCharacteristicsColumnsExist){
              datasetConfiguration.setBasicAttributesColumns(basicCharacteristicsColumns);
          }
          if(targetClassColumnExist){
              datasetConfiguration.setTargetColumn(targetClassColumn);
          }
          datasetConfiguration = datasetConfigurationRepository.save(datasetConfiguration);

          Instances finalDataset = null;
          if(!multipartFileExist){
              finalDataset = datasetService.loadDatasetInstancesByDatasetConfigurationFromMinio(datasetConfiguration);
          } else{
              finalDataset = DatasetUtil.prepareDataset(multipartFile, dataset.getFileName(), datasetConfiguration);
          }
          trainingDataInput.setDataset(finalDataset);
      } */
/* 2nd Dataset CASE - datasetConfigurationID AND OPTIONALLY ONLY one of the [basicCharacteristicsColumns, targetClassColumn]
            - if something not provided, then the already defined dataset-characteristics of DatasetConfiguration will be set
        *//*

      else if (datasetConfigurationIdExist) {
          datasetConfiguration = datasetConfigurationRepository.findById(Integer.parseInt(datasetConfigurationId)).orElseThrow(()-> new EntityNotFoundException("The Dataset Configuration you provided could not be found."));
          if(basicCharacteristicsColumnsExist){
              datasetConfiguration.setBasicAttributesColumns(basicCharacteristicsColumns);
          }else if(targetClassColumnExist){
              datasetConfiguration.setTargetColumn(targetClassColumn);
          }
          Instances finalDataset = datasetService.loadDatasetInstancesByDatasetConfigurationFromMinio(datasetConfiguration);
          trainingDataInput.setDataset(finalDataset);

      }
      trainingDataInput.setDatasetConfiguration(datasetConfiguration);
      trainingDataInput.setFilename(datasetConfiguration.getDataset().getFileName());
      //*********** END OF CONFIGURING TRAINING DATASET**************

      //*********** CONFIGURING TRAINING AlgorithmConfiguration **************
      //TODO make sure that the default options are set by initialization of the AlgorithmConfiguration Object
      //B) Algorithm config
      AlgorithmConfiguration algorithmConfiguration = null;
        */
/* 1st AlgorithmConfiguration CASE - algorithmId AND OPTIONALLY at least
            one Algorithm Option(algorithm options are crafted as a formatted String)
            - if none Algorithm Option provided, then the default will be set.
        *//*

      if(algorithmIdExist){
          Algorithm algorithm = algorithmRepository.findById(Integer.parseInt(algorithmId)).orElseThrow(() -> new EntityNotFoundException("The algorithm you provided could not be found."));
          algorithmConfiguration =  new AlgorithmConfiguration(algorithm);
          if(algorithmOptionsExist){
              algorithmConfiguration.setOptions(algorithmOptions);
          }
          algorithmConfiguration = algorithmConfigurationRepository.save(algorithmConfiguration);
      }

       */
/* 2nd AlgorithmConfiguration CASE - algorithmConfigurationId AND OPTIONALLY Options
            - if none Algorithm Option provided, then the Options of the current AlgorithmConfiguration will be set.
        *//*

      if(algorithmConfigurationIdExist){
          algorithmConfiguration = algorithmConfigurationRepository.findById(Integer.parseInt(algorithmConfigurationId)).orElseThrow(() -> new EntityNotFoundException("The algorithm configuration you provided could not be found."));
          //TODO (!)CHECK WHY: Intellij warnings that "Condition 'algorithmOptionsExist' is always 'false'" while I am getting the options from the request.
          if(algorithmOptionsExist){
              algorithmConfiguration.setOptions(algorithmOptions);
          }
      }

      trainingDataInput.setAlgorithmConfiguration(algorithmConfiguration);
    //*********** END OF CONFIGURING TRAINING AlgorithmConfiguration **************


      // 1st CASE -> ~TRAIN BASED ON OLDER TRAIN~ Provided an older Train to train the new Model based on it
      if(trainingIdExist){
        //  (MultipartFile file, String filename, DatasetConfiguration datasetConfiguration
         //We need to load the dataset based on datasetId
        if(datasetIdExist && !multipartFileExist){
                MultipartFile dataset = datasetService.getDatasetByTrainingId();
               DatasetUtil.prepareDataset(multipartFile, multipartFile.getOriginalFilename(), );
          } // new dataset was provided so we have datasetId and File
            else if(datasetIdExist && multipartFileExist){

          }

      }
      // 1st CASE -> ~TRAIN BASED ON TRAINED MODEL~ Provided an older Train to train the new Model based on it

      else if(ValidationUtil.stringExists(request.getModelId())){

      } else{

      }
  }

  //In the load methods we may need pass the user in the cases that something is uploaded first time
    // like a dataset file or an algorithm configurations
  //Load DatasetConfiguration process
  private DatasetConfiguration loadDatasetConfiguration(Integer datasetConfigurationId){

  }

  private DatasetConfiguration loadDatasetConfiguration(Integer datasetId, String basicAttributesColumns,String targetClassColumn){

  }
  private DatasetConfiguration loadDatasetConfiguration(MultipartFile file, String basicAttributesColumns,String targetClassColumn){

  }

  //Load AlgorithmConfiguration process
    private AlgorithmConfiguration loadAlgorithmConfiguration(Integer algorithmConfigurationId){

    }

    private AlgorithmConfiguration loadAlgorithmConfiguration(Integer algorithmId, String options){

    }
*/

}
