package com.backend.mlapp.service;

import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.payload.TrainingStatusResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public interface TrainingService {

    CompletableFuture<String> trainModel(TrainRequest trainingRequest) throws Exception;

    TrainingStatusResponse getTrainingStatus(Integer trainingId);
}
