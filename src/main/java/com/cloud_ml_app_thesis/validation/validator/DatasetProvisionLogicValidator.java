package com.cloud_ml_app_thesis.validation.validator;

import com.cloud_ml_app_thesis.payload.request.TrainingRequest;
import com.cloud_ml_app_thesis.validation.validation.DatasetProvisionLogicValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class DatasetProvisionLogicValidator implements ConstraintValidator<DatasetProvisionLogicValidation, TrainingRequest> {
    @Override
    public void initialize(DatasetProvisionLogicValidation constraintAnnotation){}
    @Override
    public boolean isValid(TrainingRequest dto, ConstraintValidatorContext context){
//        boolean isValid = true;

        //3 cases provided
        if(exists(dto.getDatasetId()) && exists(dto.getDatasetConfigurationId())
                && fileExists(dto.getFile())){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("You have to provide a File, or a Dataset ID or a Dataset Configuration ID.")
                    .addPropertyNode("file")
                    .addConstraintViolation();
//            isValid = false;
            return false;
        }   //Dataset ID and Dataset Case
            else if(exists(dto.getDatasetId()) && fileExists(dto.getFile())){
            context.disableDefaultConstraintViolation();;
            context.buildConstraintViolationWithTemplate("You cant provide an Uploaded Dataset ID and a Dataset.")
            .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        }  //Dataset Configuration ID and Dataset Case
        else if(exists(dto.getDatasetConfigurationId()) && fileExists(dto.getFile())){
            context.disableDefaultConstraintViolation();;
            context.buildConstraintViolationWithTemplate("You cant provide a Configured Dataset ID and a Dataset.")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        } // Dataset ID and Dataset Configuration ID
        else if(exists(dto.getDatasetId()) && exists(dto.getDatasetConfigurationId())){
            context.disableDefaultConstraintViolation();;
            context.buildConstraintViolationWithTemplate("You cant provide a Configured Dataset ID and a Dataset.")
                    .addPropertyNode("datasetId")
                    .addConstraintViolation();
            return false;
        }
        // Dataset ID and Dataset
        else if(!exists(dto.getDatasetConfigurationId()) && !exists(dto.getDatasetId()) && !fileExists(dto.getFile())){
            context.disableDefaultConstraintViolation();;
            context.buildConstraintViolationWithTemplate("You must provide a Dataset or an uploaded Dataset ID or a Dataset Configuration ID.")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        }

        if(attributesExist(dto.getBasicCharacteristicsColumns(), dto.getTargetClassColumn())){
            if(exists(dto.getDatasetId())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("You can't provide Dataset ID and dataset attributes.")
                        .addPropertyNode("datasetId")
                        .addConstraintViolation();
                return false;
            } else if(exists(dto.getDatasetConfigurationId())){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("You can't provide Dataset Configuration Id and dataset attributes.")
                        .addPropertyNode("datasetConfigurationId")
                        .addConstraintViolation();
                return false;
            }
        }


        return true;
    }

    private boolean exists(String parameter){
        return parameter!= null && !parameter.isEmpty() && !parameter.isBlank();
    }

    private boolean fileExists(MultipartFile file){
        return file != null && !file.isEmpty();
    }

    private boolean attributesExist(String basicAttributesColumns, String targetClassColumn){
        return exists(basicAttributesColumns) || exists(targetClassColumn);
    }

}
