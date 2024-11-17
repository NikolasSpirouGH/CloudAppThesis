package com.cloud_ml_app_thesis.entity;
import org.springframework.web.multipart.MultipartFile;

public class TrainingDataInput {
    Training training;
    MultipartFile dataset;
    String filename;
    DatasetConfiguration datasetConfiguration;
    String algorithmClassName; 
    AlgorithmConfiguration algorithmConfiguration;
}
