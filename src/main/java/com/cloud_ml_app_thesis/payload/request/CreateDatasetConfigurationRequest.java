package com.cloud_ml_app_thesis.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NonNull
@Setter
@Getter
public class CreateDatasetConfigurationRequest {
    @NonNull
    private Integer datasetId;
    private String basicAttributesColumns;
    private String targetColumn;

}
