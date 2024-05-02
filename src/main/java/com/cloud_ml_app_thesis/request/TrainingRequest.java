package com.cloud_ml_app_thesis.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TrainingRequest {
    private String classifier;
    private String options;
    private String fileUrl;
}
