package com.backend.mlapp.exception;

public class AlgorithmParameterSettingException extends RuntimeException {
    public AlgorithmParameterSettingException(String message) {
        super(message);
    }

    public AlgorithmParameterSettingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
