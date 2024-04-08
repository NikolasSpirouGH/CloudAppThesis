package com.backend.mlapp.exception;

    public class DatasetLoadException extends RuntimeException {
    public DatasetLoadException(String message, Throwable e) {
        super(message, e);
    }
}
