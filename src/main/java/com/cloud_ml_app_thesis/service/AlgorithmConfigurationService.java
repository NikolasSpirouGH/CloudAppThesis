package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.entity.AlgorithmConfiguration;
import com.cloud_ml_app_thesis.repository.AlgorithmConfigurationRepository;
import com.cloud_ml_app_thesis.repository.AlgorithmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlgorithmConfigurationService {

    private AlgorithmRepository algorithmRepository;
    private AlgorithmConfigurationRepository algorithmConfigurationRepository;
    public Integer createAlgorithmConfiguration(Integer algorithmId, String options){
        Optional<Algorithm> algorithm = algorithmRepository.findById(algorithmId);
        AlgorithmConfiguration algorithmConfiguration = new AlgorithmConfiguration();
        algorithmConfiguration.setOptions(options);
        if(algorithm.isPresent()){
            algorithmConfiguration.setAlgorithm(algorithm.get());
            algorithmConfiguration = algorithmConfigurationRepository.save(algorithmConfiguration);
            return algorithmConfiguration.getId();
        } else {
            return null;
        }
    }
}
