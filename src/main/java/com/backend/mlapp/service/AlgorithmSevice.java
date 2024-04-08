package com.backend.mlapp.service;

import com.backend.mlapp.entity.Algorithm;
import org.springframework.stereotype.Service;

import java.util.List;


public interface AlgorithmSevice {
    List<Algorithm> getAllAlgorithms();

    Algorithm getAlgorithmByName(String name);

    void updateAlgorithmParameters(String name, String parametersJson);
}
