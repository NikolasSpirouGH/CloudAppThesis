package com.backend.mlapp.exception;

public class TrainingModelException extends RuntimeException {

    public TrainingModelException(String message) {
        super(message);
    }

    public TrainingModelException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrainingModelException(Throwable cause) {
        super(cause);
    }
}
