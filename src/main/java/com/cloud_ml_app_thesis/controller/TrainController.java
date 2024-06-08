package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.entity.Training;
import com.cloud_ml_app_thesis.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("api/train")
@Slf4j
@CrossOrigin(origins = "*")
public class TrainController {
    private final TrainService trainService;

    @Autowired
    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PostMapping("/trainDataset")
    public ResponseEntity<Integer> trainModel(@RequestParam("algorithmConfId") Integer algorithmConfId,
                                           @RequestParam("datasetConfId") Integer datasetConfId) {
        Integer trainingId = trainService.createTrainingRequest(algorithmConfId, datasetConfId);
        return ResponseEntity.ok(trainingId);
    }

    @GetMapping("/train/status/{trainingId}")
    public ResponseEntity<Training> checkTraining(@PathVariable Integer trainingId) {
        Training status = trainService.checkTraining(trainingId);
        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/get-trainings")
    public ResponseEntity<List<Training>> getTrainings() {
        List<Training> trainings = trainService.getTrainings();
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }

}
