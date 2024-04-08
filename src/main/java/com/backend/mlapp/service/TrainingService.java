package com.backend.mlapp.service;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.entity.Training;
import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.payload.TrainingStatusResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TrainingService {

    CompletableFuture<String> trainModel(AppUser userId, TrainRequest trainingRequest) throws Exception;

    TrainingStatusResponse getTrainingStatus(Integer trainingId);

    List<Training> getTrainingsByCriteria(Integer userId, LocalDate startDate, LocalDate endDate, String algorithm);
}
