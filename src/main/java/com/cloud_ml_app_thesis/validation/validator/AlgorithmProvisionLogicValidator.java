package com.cloud_ml_app_thesis.validation.validator;

import com.cloud_ml_app_thesis.payload.request.TrainingRequest;
import com.cloud_ml_app_thesis.validation.validation.AlgorithmProvisionLogicValidation;
import com.cloud_ml_app_thesis.validation.validation.DatasetProvisionLogicValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class AlgorithmProvisionLogicValidator implements ConstraintValidator<AlgorithmProvisionLogicValidation, TrainingRequest> {
    @Override
    public void initialize(AlgorithmProvisionLogicValidation constraintAnnotation){}
    @Override
    public boolean isValid(TrainingRequest dto, ConstraintValidatorContext context) {
//        boolean isValid = true;

        //3 cases provided
        if (exists(dto.getAlgorithmConfigurationId()) && exists(dto.getAlgorithmOptions())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("You have to provide Algorithm Options, or Algorithm Configuration Id or nothing. You cant provide both.")
                    .addPropertyNode("algorithmOptions")
                    .addConstraintViolation();
//            isValid = false;
            return false;
        }
        return true;
    }
    private boolean exists(String parameter){
        return parameter!= null && !parameter.isEmpty() && !parameter.isBlank();
    }


}
