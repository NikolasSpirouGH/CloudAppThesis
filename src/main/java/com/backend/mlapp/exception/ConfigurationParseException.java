package com.backend.mlapp.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ConfigurationParseException extends RuntimeException {
    public ConfigurationParseException(String failedToParseAlgorithmConfigs, JsonProcessingException e) {
        super(failedToParseAlgorithmConfigs,e);
    }
}
