package com.backend.mlapp.controllers;

import com.backend.mlapp.entity.Algorithm;
import com.backend.mlapp.service.AlgorithmSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
@RequiredArgsConstructor
public class AlgorithmController {

    private final AlgorithmSevice algorithmService;

    @GetMapping("/list-algorithms")
    public ResponseEntity<List<Algorithm>> listAlgorithms() {
        List<Algorithm> algorithms = algorithmService.getAllAlgorithms();
        return ResponseEntity.ok(algorithms);
    }

    @GetMapping("/{name}/parameters")
    public ResponseEntity<String> getDefaultParameters(@PathVariable String name) {
        Algorithm algorithm = algorithmService.getAlgorithmByName(name);
        return ResponseEntity.ok(algorithm.getDefaultParameters());
    }

    @PostMapping("/{name}/parameters")
    public ResponseEntity<?> updateAlgorithmParameters(@PathVariable String name, @RequestBody String parametersJson) {
        algorithmService.updateAlgorithmParameters(name, parametersJson);
        return ResponseEntity.ok().build();
    }
}
