package com.backend.mlapp.service;

import com.backend.mlapp.repository.AlgorithmRepository;
import com.backend.mlapp.repository.TrainingRepository;
import com.backend.mlapp.config.FileManager;
import com.backend.mlapp.entity.Algorithm;
import com.backend.mlapp.exception.ResourceNotFoundException;
import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.service.impl.service.TrainingServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrainingServiceImplTest {

    @Autowired
    private TrainingService trainingService;
    @Mock
    private AlgorithmRepository algorithmRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private FileManager fileManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void trainModel_WhenAlgorithmExists() throws Exception {
        TrainRequest trainRequest = new TrainRequest();
        trainRequest.setFile();
        trainRequest.setAlgorithm("NAIVE-BAYES");
        Algorithm algorithm = new Algorithm(); // Assume Algorithm entity setup here
        when(algorithmRepository.findByName(anyString())).thenReturn(Optional.of(algorithm));
        CompletableFuture<String> result = trainingService.trainModel(trainRequest);
        assertNotNull(result);
    }

    @Test
    void trainModel_WhenAlgorithmDoesNotExist() {
        // Setup
        TrainRequest trainRequest = new TrainRequest();
        trainRequest.setAlgorithm("NonExistentAlgorithm");
        when(algorithmRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            CompletableFuture<String> result = trainingService.trainModel(trainRequest);
            result.join(); // This is necessary because the exception will be thrown when the future completes.
        });
    }

    // Additional tests can be added here
}