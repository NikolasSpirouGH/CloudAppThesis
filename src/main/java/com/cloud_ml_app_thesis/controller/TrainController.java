package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.entity.Training;
import com.cloud_ml_app_thesis.payload.response.ErrorResponse;
import com.cloud_ml_app_thesis.payload.request.TrainingRequest;
import com.cloud_ml_app_thesis.payload.response.CustomResponse;
import com.cloud_ml_app_thesis.payload.response.DataMapResponse;
import com.cloud_ml_app_thesis.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

//    @GetMapping("/create-train")
//    ResponseEntity<Map<String, String>> createTrain(){
//        try {
//            Training training = new Training();
//            Integer id = trainService.createTrain(training);
//            if (id == null || id == -1) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("errorMessage", "Training couldn't be created. If this message occurs, please contact the IT support."));
//            }
//            return ResponseEntity.ok(Collections.singletonMap("id", id.toString()));
//        } catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("errorMessage", "Internal server error."));
//        }
//
//    }
    @PostMapping("/train-model")
    public ResponseEntity<CustomResponse> trainModel(@ModelAttribute TrainingRequest request) {


        MultipartFile file = request.getFile();

        try {
            /*Path tempFile = Files.createTempFile("upload_", file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, starnd)*/
            CustomResponse response = trainService.startTraining(file, request);
            if(response instanceof DataMapResponse) {
                return ResponseEntity.ok().body((DataMapResponse) response);
            }
            return ResponseEntity.internalServerError().body(new ErrorResponse("Unexpected error while trying to start your model training."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Unexpected error while trying to start your model training."));
        }
    }

//    @PostMapping("/train-model-based-on-train")
//    public ResponseEntity<CustomResponse> trainModelB

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
