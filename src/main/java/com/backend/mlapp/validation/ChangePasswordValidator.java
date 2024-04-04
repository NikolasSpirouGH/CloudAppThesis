package com.backend.mlapp.validation;

import com.backend.mlapp.payload.ChangePasswordRequest;
import com.backend.mlapp.payload.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ChangePasswordValidator implements ConstraintValidator<ValidPassword, Object> {

    private Pattern pattern;
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-zA-Z]).{9,}$";

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        ChangePasswordRequest request = (ChangePasswordRequest) obj;
     /*   if (request.getPassword() == null || request.getConfirmPassword() == null) {
            return false;
        }*/

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode("confirmPassword").addConstraintViolation();
            return false;
        }

        if (pattern.matcher(request.getNewPassword()).matches()) {
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must be at least 8 characters long and contain both letters and digits")
                    .addPropertyNode("password").addConstraintViolation();
            return false;
        }
    }
}