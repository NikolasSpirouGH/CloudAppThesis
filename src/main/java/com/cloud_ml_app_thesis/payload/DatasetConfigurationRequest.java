package com.cloud_ml_app_thesis.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NonNull
@Setter
@Getter
public class DatasetConfigurationRequest {
    @NonNull
    private String fileUrl;
    private String basicAttributesColumns;
    private String targetColumn;

}
