package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.entity.AlgorithmConfiguration;
import com.cloud_ml_app_thesis.repository.AlgorithmConfigurationRepository;
import com.cloud_ml_app_thesis.repository.AlgorithmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlgorithmService {

    private final AlgorithmRepository algorithmRepository;
    private final AlgorithmConfigurationRepository algorithmConfigurationRepository;

    public List<Algorithm> getAlgorithms() {
        return algorithmRepository.findAll();
    }

    public void chooseAlgorithm(String name, String options) {
        Algorithm algorithm = algorithmRepository.findByName(name);
        if(algorithm == null) {
            //TODO PROPER EXCEPTIONS AND LOGGING
            throw new NullPointerException();
        }
        AlgorithmConfiguration algorithmConfiguration = new AlgorithmConfiguration();
        algorithmConfiguration.setOptions(options);
        algorithmConfiguration.setAlgorithm(algorithm);

        algorithmConfigurationRepository.save(algorithmConfiguration);
    }
}
