package com.cloud_ml_app_thesis.validation.validation;


import com.cloud_ml_app_thesis.validation.validator.AlgorithmProvisionLogicValidator;
import com.cloud_ml_app_thesis.validation.validator.DatasetProvisionLogicValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AlgorithmProvisionLogicValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AlgorithmProvisionLogicValidation {
    String message() default  "Not valid Algorithm provision in your Training Request.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
