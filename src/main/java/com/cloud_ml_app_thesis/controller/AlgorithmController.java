package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.service.AlgorithmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
public class AlgorithmController {

    private final AlgorithmService algorithmSevice;

    @GetMapping("/get-algorithms")
    public ResponseEntity<List<Algorithm>> getAlgorithms() {
        return ResponseEntity.ok(algorithmSevice.getAlgorithms());
    }

    @PostMapping("/choose-algorithm")
    public ResponseEntity<String> chooseAlgorithm(@RequestParam Integer id, @RequestParam(required= false) String options) {
        algorithmSevice.chooseAlgorithm(id, options);
        return new ResponseEntity<>("Algorithm configuration created.", HttpStatus.CREATED);
    }

}
