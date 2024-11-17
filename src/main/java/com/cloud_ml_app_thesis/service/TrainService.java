package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.dto.train.MyTrainingDTO;
import com.cloud_ml_app_thesis.entity.*;
import com.cloud_ml_app_thesis.enumeration.TrainingStatus;
import com.cloud_ml_app_thesis.exception.AlgorithmNotFoundException;
import com.cloud_ml_app_thesis.exception.UserNotFoundException;
import com.cloud_ml_app_thesis.payload.request.TrainingRequest;
import com.cloud_ml_app_thesis.payload.response.*;
import com.cloud_ml_app_thesis.repository.*;

import com.cloud_ml_app_thesis.util.AlgorithmUtil;
import com.cloud_ml_app_thesis.util.DatasetUtil;
import com.cloud_ml_app_thesis.util.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TrainService {

    @Autowired
    private final TrainRepository trainRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final AlgorithmConfigurationRepository algorithmConfigurationRepository;

    @Autowired
    private final AlgorithmRepository algorithmRepository;


    @Autowired
    private final DatasetConfigurationRepository datasetConfigurationRepository;

    @Autowired
    private final ModelService modelService;

    @Autowired
    private final DatasetService datasetService;

    @Autowired
    private final AlgorithmService algorithmService;

    @Autowired
    private final ObjectMapper objectMapper;


    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);

    public Integer createTrain(Training training){
        training = trainRepository.save(training);
        if(training.getId() != null){
            return training.getId();
        }
        return  -1;
    }

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
    public CustomResponse startTraining(MultipartFile file, TrainingRequest request) {

        Training training = new Training();
        training.setStatus(TrainingStatus.REQUESTED);

        String algorithmId = request.getAlgorithmId();

        Optional<Algorithm> algorithmOptional = algorithmRepository.findById(Integer.parseInt(algorithmId));
        AlgorithmConfiguration algorithmConfiguration = new AlgorithmConfiguration();
        if(algorithmOptional.isPresent()){
            algorithmConfiguration.setAlgorithm(algorithmOptional.get());
        } else {
           /* training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);*/
            throw new AlgorithmNotFoundException("Algorithm could not be found");
        }

        Optional<User> user = userRepository.findByUsername("nickriz");
        if(user.isEmpty()){
           /* training.setStatus(TrainingStatus.FAILED);
            trainRepository.save(training);*/
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
        training.setStatus(TrainingStatus.REQUESTED);
        training.setStartedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
//            trainRepository.save(training);

        //TODO try catch for train in order to save training with correct status



            try {
                training = trainRepository.save(training);
            } catch(DataAccessException e){
                logger.error("Failed to save DatasetConfiguration", e);
                throw e;
            }
            train(training, file, dataset.getFileName(), datasetConfiguration, algorithmOptional.get().getClassName(), algorithmConfiguration);
            return new DataResponse("Your model with id '"+training.getId() +"' is being training!",Collections.singletonMap("id", training.getId()));

    }


    @Async
    public CompletableFuture<Void> train(Training training, MultipartFile dataset, String filename, DatasetConfiguration datasetConfiguration, String algorithmClassName, AlgorithmConfiguration algorithmConfiguration) {
        return CompletableFuture.runAsync(() -> {

            try {



                Instances data = DatasetUtil.prepareDataset(dataset, filename, datasetConfiguration);
                //data = datasetService.selectColumns(data, datasetConfiguration.getBasicAttributesColumns(), datasetConfiguration.getTargetColumn());

                boolean isClassifier = AlgorithmUtil.isClassifier(algorithmClassName);
                boolean isClusterer = AlgorithmUtil.isClusterer(algorithmClassName);


                //TODO see the save for finished date to not be that many saves
                if (isClassifier) {
                    training.setStatus(TrainingStatus.RUNNING);
                    trainRepository.save(training);
                    Classifier cls = trainClassifier(training, algorithmClassName, algorithmConfiguration, data);
                    training.setStatus(TrainingStatus.COMPLETED);
                    training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
                    trainRepository.save(training);
                    evaluateAndSaveClassifier(training, cls, data);
                } else if (isClusterer) {
                    training.setStatus(TrainingStatus.RUNNING);
                    trainRepository.save(training);
                    Clusterer clus = trainClusterer(training, algorithmClassName, algorithmConfiguration, data);
                    training.setStatus(TrainingStatus.COMPLETED);
                    training.setFinishedDate(ZonedDateTime.now(ZoneId.of("Europe/Athens")));
                    trainRepository.save(training);
                    evaluateAndSaveClusterer(training, clus, data);
                } else {
                    logger.error("Unsupported algorithm type for algorithmClassName: {}", algorithmClassName);
                    training.setStatus(TrainingStatus.FAILED);
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
        logger.info("Dataset File URL: {}", datasetConfiguration.getDataset().getFilePath());
        training.setDatasetConfiguration(datasetConfiguration);
        return true;
    }

    private Instances prepareData(DatasetConfiguration datasetConfiguration) throws Exception {
        return datasetService.loadDataset(datasetConfiguration);
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
    public CustomResponse getTrainings(String username, TrainingStatus status) {

        try {
            Optional<List<Training>> trainings = trainRepository.findAllByUserUsernameAndStatus(username, status);
            if(trainings == null || trainings.isEmpty()){
                return new InformationResponse("Trainings for user '" + username + "' could not be found.");


            }
            return new SingleObjectDataResponse(trainings);
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
              return new SingleObjectDataResponse(trainingOptional.get());
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

  private Object findTrainCase(TrainingRequest request, boolean multipartFileExists, MultipartFile multipartFile, String username){
      Instances

      String datasetId = request.getDatasetId();
      boolean datasetIdExist = ValidationUtil.stringExists(datasetId);

      String datasetConfigurationId = request.getDatasetConfigurationId();
      boolean datasetConfigurationIdExist = ValidationUtil.stringExists(datasetConfigurationId);

      String basicCharacteristicsColumns = request.getBasicCharacteristicsColumns();
      boolean basicCharacteristicsColumnsExist = ValidationUtil.stringExists(basicCharacteristicsColumns);

      String targetClassColumn = request.getTargetClassColumn();
      boolean targetClassColumnExist = ValidationUtil.stringExists(targetClassColumn);

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
      if(datasetIdExist && multipartFileExists){
          return new ErrorResponse("You can't train a model with an already uploaded Dataset and a new Dataset File at the same time.");
      }

      // 3rd check - Can't provide datasetId and datasetConfigurationId at the same time
      if(datasetIdExist && datasetConfigurationIdExist){
          return new ErrorResponse("You can't train a model providing a datasetId and a datasetConfigurationId at the same time.");
      }
      // 4rth check - Can't provide algorithmId and algorithmConfigurationId at the same time
      if(algorithmOptionsExist && algorithmConfigurationIdExist){
          return new ErrorResponse("You can't train a model providing a algorithmId and a algorithmConfigurationId at the same time.");
      }

      // 5th check - If MultiPartFile is provided then upload the dataset and get the datasetId to continue
      if(multipartFileExists){
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

      // 6th check if the user provided an older Train to train the new Model based on it
      if(trainingIdExist){
        //  (MultipartFile file, String filename, DatasetConfiguration datasetConfiguration
         //We need to load the dataset based on datasetId
        if(datasetIdExist && !multipartFileExists){
                MultipartFile dataset = datasetService.getDatasetByTrainingId();
               DatasetUtil.prepareDataset(trainingId);
          } // new dataset was provided so we have datasetId and File
            else if(datasetIdExist && multipartFileExists){

          }

      } else if(ValidationUtil.stringExists(request.getModelId())){

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

}
