package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.payload.request.TrainingRequest;
import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import com.cloud_ml_app_thesis.payload.response.ErrorResponse;
import com.cloud_ml_app_thesis.payload.response.ErrorStatusResponse;
import com.cloud_ml_app_thesis.payload.response.IdResponse;
import com.cloud_ml_app_thesis.repository.*;
import com.cloud_ml_app_thesis.repository.status.TrainingStatusRepository;
import com.cloud_ml_app_thesis.service.AlgorithmService;
import com.cloud_ml_app_thesis.service.DatasetService;
import com.cloud_ml_app_thesis.service.ModelService;
import com.cloud_ml_app_thesis.util.DatasetUtil;
import com.cloud_ml_app_thesis.util.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import weka.core.Instances;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
@Service
public class TrainingRequestHelper extends TrainingRequest {

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

    private boolean multipartFileExist;
    private boolean datasetIdExist;
    private boolean datasetConfigurationIdExist;
    private boolean basicCharacteristicsColumnsExist;
    private boolean targetClassColumnExist;
    private boolean algorithmIdExist;
    private boolean algorithmOptionsExist;
    private boolean algorithmConfigurationIdExist;
    private boolean trainingIdExist;
    private boolean modelIdExist;

    @Autowired
    public TrainingRequestHelper(TrainRepository trainRepository, UserRepository userRepository, AlgorithmConfigurationRepository algorithmConfigurationRepository, AlgorithmRepository algorithmRepository, TrainingStatusRepository trainingStatusRepository, DatasetRepository datasetRepository, DatasetConfigurationRepository datasetConfigurationRepository, ModelService modelService, DatasetService datasetService, AlgorithmService algorithmService, TrainingRequest trainingRequest){
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
        this.algorithmConfigurationRepository = algorithmConfigurationRepository;
        this.algorithmRepository = algorithmRepository;
        this.trainingStatusRepository = trainingStatusRepository;
        this.datasetRepository = datasetRepository;
        this.datasetConfigurationRepository = datasetConfigurationRepository;
        this.modelService = modelService;
        this.datasetService = datasetService;
        this.algorithmService = algorithmService;

        this.setFile(trainingRequest.getFile());
        this.multipartFileExist = ValidationUtil.multipartFileExist(trainingRequest.getFile());

        this.setDatasetId(trainingRequest.getDatasetId());
        this.datasetIdExist = ValidationUtil.stringExists(trainingRequest.getDatasetId());

        this.setDatasetConfigurationId(trainingRequest.getDatasetConfigurationId());
        this.datasetConfigurationIdExist=ValidationUtil.stringExists(trainingRequest.getDatasetConfigurationId());

        this.setBasicCharacteristicsColumns(trainingRequest.getBasicCharacteristicsColumns());
        this.basicCharacteristicsColumnsExist = ValidationUtil.stringExists(trainingRequest.getBasicCharacteristicsColumns());

        this.setTargetClassColumn(trainingRequest.getTargetClassColumn());
        this.targetClassColumnExist = ValidationUtil.stringExists(trainingRequest.getTargetClassColumn());

        this.setAlgorithmId(trainingRequest.getAlgorithmId());
        this.algorithmIdExist = ValidationUtil.stringExists(trainingRequest.getAlgorithmId());

        this.setAlgorithmOptions(trainingRequest.getAlgorithmOptions());
        this.algorithmOptionsExist = ValidationUtil.stringExists(trainingRequest.getAlgorithmOptions());

        this.setAlgorithmConfigurationId(trainingRequest.getAlgorithmConfigurationId());
        this.algorithmConfigurationIdExist = ValidationUtil.stringExists(trainingRequest.getAlgorithmConfigurationId());

        this.setTrainingId(trainingRequest.getTrainingId());
        this.trainingIdExist = ValidationUtil.stringExists(trainingRequest.getTrainingId());

        this.setModelId(trainingRequest.getModelId());
        this.modelIdExist = ValidationUtil.stringExists(trainingRequest.getModelId());
    }

    public TrainingDataInput configureTrainingDataInput() throws Exception {
        TrainingDataInput trainingDataInput = new TrainingDataInput();
        DatasetConfiguration datasetConfiguration = null;
        // 1st check - Can't provide trainingId and modelId at the same time
        if(trainingIdExist && modelIdExist){
            trainingDataInput.setErrorResponse(new ErrorResponse("You can't train a model based on a Training and a Model at the same time."));
            return trainingDataInput;
        }

        // 2nd check - Can't provide datasetId and a new File
        if(datasetIdExist && multipartFileExist){
            trainingDataInput.setErrorResponse(new ErrorResponse("You can't train a model with an already uploaded Dataset and a new Dataset File at the same time."));
            return trainingDataInput;
        }

        // 3rd check - Can't provide datasetId and datasetConfigurationId at the same time
        if(datasetIdExist && datasetConfigurationIdExist){
            trainingDataInput.setErrorResponse(new ErrorResponse("You can't train a model providing a datasetId and a datasetConfigurationId at the same time."));
            return trainingDataInput;
        }

        // 4th check - Can't provide datasetId and datasetConfigurationId at the same time
        if(datasetConfigurationIdExist && basicCharacteristicsColumnsExist && targetClassColumnExist){
            trainingDataInput.setErrorResponse(new ErrorResponse("You can't train a model providing a datasetConfigurationId and booth basic characteristics columns and target class column at the same time."));
            return trainingDataInput;
        }

        // 5th check - Can't provide algorithmId and algorithmConfigurationId at the same time
        if(algorithmIdExist && algorithmConfigurationIdExist){
            trainingDataInput.setErrorResponse(new ErrorResponse("You can't train a model providing a algorithmId and a algorithmConfigurationId at the same time."));
            return trainingDataInput;
        }
        // 6th check - Can't provide algorithmId and algorithmConfigurationId at the same time
        if(algorithmConfigurationIdExist && algorithmOptionsExist){
            trainingDataInput.setErrorResponse(new ErrorResponse("You can't train a model providing algorithm options and a algorithmConfigurationId at the same time."));
            return trainingDataInput;
        }

        // 7th check - If MultiPartFile is provided then upload the dataset and get the datasetId to continue
        if(multipartFileExist){
            CustomResponse uploadFileResponse = datasetService.uploadDataset(this.getFile(), this.getUsername());
            if(uploadFileResponse instanceof IdResponse){
                this.setDatasetId(((IdResponse)uploadFileResponse).getId());
                datasetIdExist = true;
            } else if (uploadFileResponse instanceof ErrorResponse) {
                trainingDataInput.setErrorResponse((ErrorResponse) uploadFileResponse);
            } else{
                trainingDataInput.setErrorResponse(new ErrorStatusResponse("Unexpected Error while trying to upload the File.", HttpStatus.INTERNAL_SERVER_ERROR));
            }
        }

        //*********** CONFIGURING TRAINING DATASET**************
        //A) DATASET config
        /* 1st Dataset CASE - datasetId-Or-MultipartFile AND OPTIONALLY at least one of the [basicCharacteristicsColumns, targetClassColumn]
            - if not provided, then last column is the target class and all the previous columns are basic characteristics
        */
        
        if(datasetIdExist){
            //first set the DatasetConfiguration
            //TODO change the Exception message
            datasetConfiguration = new DatasetConfiguration();
            Dataset dataset = datasetRepository.findById(Integer.parseInt(this.getDatasetId())).orElseThrow(() -> new EntityNotFoundException("The dataset for your training could not be found!"));
            datasetConfiguration.setDataset(dataset);
            if(basicCharacteristicsColumnsExist){
                datasetConfiguration.setBasicAttributesColumns(this.getBasicCharacteristicsColumns());
            }
            if(targetClassColumnExist){
                datasetConfiguration.setTargetColumn(this.getTargetClassColumn());
            }
            datasetConfiguration = datasetConfigurationRepository.save(datasetConfiguration);

            Instances finalDataset = null;
            if(!multipartFileExist){
                finalDataset = datasetService.loadDatasetInstancesByDatasetConfigurationFromMinio(datasetConfiguration);
            } else{
                finalDataset = DatasetUtil.prepareDataset(this.getFile(), dataset.getFileName(), datasetConfiguration);
            }
            trainingDataInput.setDataset(finalDataset);
        } /* 2nd Dataset CASE - datasetConfigurationID AND OPTIONALLY ONLY one of the [basicCharacteristicsColumns, targetClassColumn]
            - if something not provided, then the already defined dataset-characteristics of DatasetConfiguration will be set
        */
        else if (datasetConfigurationIdExist) {
            datasetConfiguration = datasetConfigurationRepository.findById(Integer.parseInt(this.getDatasetConfigurationId())).orElseThrow(()-> new EntityNotFoundException("The Dataset Configuration you provided could not be found."));
            if(basicCharacteristicsColumnsExist){
                datasetConfiguration.setBasicAttributesColumns(this.getBasicCharacteristicsColumns());
            }else if(targetClassColumnExist){
                datasetConfiguration.setTargetColumn(this.getTargetClassColumn());
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
        /* 1st AlgorithmConfiguration CASE - algorithmId AND OPTIONALLY at least
            one Algorithm Option(algorithm options are crafted as a formatted String)
            - if none Algorithm Option provided, then the default will be set.
        */
        if(algorithmIdExist){
            Algorithm algorithm = algorithmRepository.findById(Integer.parseInt(this.getAlgorithmId())).orElseThrow(() -> new EntityNotFoundException("The algorithm you provided could not be found."));
            algorithmConfiguration =  new AlgorithmConfiguration(algorithm);
            if(algorithmOptionsExist){
                algorithmConfiguration.setOptions(this.getAlgorithmOptions());
            }
            algorithmConfiguration = algorithmConfigurationRepository.save(algorithmConfiguration);
        }

       /* 2nd AlgorithmConfiguration CASE - algorithmConfigurationId AND OPTIONALLY Options
            - if none Algorithm Option provided, then the Options of the current AlgorithmConfiguration will be set.
        */
        if(algorithmConfigurationIdExist){
            algorithmConfiguration = algorithmConfigurationRepository.findById(Integer.parseInt(this.getAlgorithmConfigurationId())).orElseThrow(() -> new EntityNotFoundException("The algorithm configuration you provided could not be found."));
            //TODO (!)CHECK WHY: Intellij warnings that "Condition 'algorithmOptionsExist' is always 'false'" while I am getting the options from the request.
            if(algorithmOptionsExist){
                algorithmConfiguration.setOptions(this.getAlgorithmOptions());
            }
        }

        trainingDataInput.setAlgorithmConfiguration(algorithmConfiguration);
        //*********** END OF CONFIGURING TRAINING AlgorithmConfiguration **************
    }

}
