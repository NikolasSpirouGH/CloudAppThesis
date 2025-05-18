package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.response.MyResponse;
import com.cloud_ml_app_thesis.entity.Training;

import com.cloud_ml_app_thesis.dto.request.training.TrainingStartRequest;

import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/train")
@Slf4j
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TrainController {
    private final TrainService trainService;
    private final UserRepository userRepository;

    @PostMapping("/train-model")
    public ResponseEntity<MyResponse<?>> trainModel(@ModelAttribute TrainingStartRequest request) {

        MultipartFile file = request.getFile();

        try {
            User user = userRepository.findByUsername("bigspy").orElseThrow();
            MyResponse<?> response = trainService.startTraining(request, user);
            if(response.getMessage() != null) {
                return ResponseEntity.internalServerError().body(response);
            }
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
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
/*
    @GetMapping("/get-trainings")
    public ResponseEntity<List<Training>> getTrainings() {
        List<Training> trainings = trainService.getTrainings();
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }*/

}
