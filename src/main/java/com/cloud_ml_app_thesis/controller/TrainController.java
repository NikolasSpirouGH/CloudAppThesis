package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.dto.response.ApiResponse;
import com.cloud_ml_app_thesis.entity.Training;

import com.cloud_ml_app_thesis.dto.request.training.TrainingStartRequest;

import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.repository.UserRepository;
import com.cloud_ml_app_thesis.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<ApiResponse<?>> trainModel(@ModelAttribute TrainingStartRequest request) {


        MultipartFile file = request.getFile();

        try {
            /*Path tempFile = Files.createTempFile("upload_", file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, starnd)*/
            User user = userRepository.findByUsername("bigspy").orElseThrow();
            ApiResponse<?> response = trainService.startTraining(request, user);
            if(response.getMessage() != null) {
                return ResponseEntity.internalServerError().body(response);
            }
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
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
