package com.backend.mlapp.service.impl.service;

import com.backend.mlapp.entity.Algorithm;
import com.backend.mlapp.repository.AlgorithmRepository;
import com.backend.mlapp.service.AlgorithmSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmSevice {

    private final AlgorithmRepository algorithmRepository;

    @Override
    public List<Algorithm> getAllAlgorithms() {
        return  algorithmRepository.findAll();
    }

    @Override
    public Algorithm getAlgorithmByName(String name) {
        return null;
    }

    @Override
    public void updateAlgorithmParameters(String name, String parametersJson) {

    }
}
