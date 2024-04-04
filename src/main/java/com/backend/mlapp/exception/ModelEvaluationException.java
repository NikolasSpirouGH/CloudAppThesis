package com.backend.mlapp.exception;

public class ModelEvaluationException extends RuntimeException {
    public ModelEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
