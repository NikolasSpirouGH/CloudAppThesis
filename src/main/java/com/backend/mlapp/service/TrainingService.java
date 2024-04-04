package com.backend.mlapp.service;

import com.backend.mlapp.payload.TrainRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public interface TrainingService {

    public CompletableFuture<String> trainModel(TrainRequest trainingRequest) throws Exception;

    //String saveModelToMinIO(String trainingId) throws IOException;
}
