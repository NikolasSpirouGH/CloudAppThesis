package com.cloud_ml_app_thesis.payload.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StartTrainingRequest {
    private Integer trainingId;
    private Integer datasetConfigurationId;
    private Integer algorithmConfigurationId;
}
