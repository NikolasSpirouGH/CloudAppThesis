package com.cloud_ml_app_thesis.payload.request;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateAlgorithmConfigurationRequest {
    private Integer algorithmId;
    private String options;
}
