package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @PostMapping("/predict")
    public List<Double> predict(@RequestParam Integer modelId, @RequestParam Integer datasetId) throws Exception {
      return  modelService.predict(modelId, datasetId);

    }
}
