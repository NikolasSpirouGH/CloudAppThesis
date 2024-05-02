package com.cloud_ml_app_thesis.controller;

import com.cloud_ml_app_thesis.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.converters.ConverterUtils;

import java.util.*;


@RestController
@RequestMapping("/invoicesWS")

@Slf4j
@CrossOrigin(origins = "*")
public class TrainController {

    private TrainService trainService;


    @Autowired
    public TrainController(TrainService trainService) {
        this.trainService = trainService;

    }

    @GetMapping(value = "/getWekaAlgorithmsInfos", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getTotalNumberInvoices() {

        return ResponseEntity.ok().body(trainService.getWekaAlgorithms());

    }

    @GetMapping(value = "/getClassifierOptions/{classifierName}", produces = "application/json;charset=UTF-8")
    public String getClassifierOptions(@PathVariable String classifierName){
        return trainService.getClassifierOptions(classifierName);
    }

    @GetMapping(value = "/getClassifierObjectByName/{classifierName}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> getClassifierObjectByName(@PathVariable String classifierName){
        try {
            return ResponseEntity.ok().body(trainService.findClassifierByName(classifierName).toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
            //throw new RuntimeException(e);
        }
    }
//
//    @PostMapping("/train")
//    public ResponseEntity<String> trainModel(@RequestBody TrainingRequest request){
//    return null;
//    }

    @PostMapping("/train")
    public ResponseEntity<String> trainModel(@RequestParam("classifier") String classifierName,
                                             @RequestParam("options") String options,
                                             @RequestParam("file") MultipartFile file) {
        try {
            Classifier cls = trainService.findClassifierByName(classifierName);
//            //"-C", "0.25", "-M", "2"
            //String optionsStr = "-C: 1.0, -L: 0.001, -P: 1.0E-12, -N: 0";
            //String optionsStr = "-C: 0.25, -M:2";
            String[] arr =  options.trim().split(",");
            ArrayList<String> finalOptions = new ArrayList<>();
            for(String s : arr){
                if(s.contains(":")) {
                    String[] tempArr = s.split(":");
                    for(String s2 : tempArr) {
                        finalOptions.add(s2.trim());
                    }
                } else{
                    finalOptions.add(s);
                }

            }
            String[] finalOptionsArr = (String[]) finalOptions.toArray(new String[0]);

            System.out.println("The OptionsList is:");
            System.out.println(finalOptions);
            System.out.println("The Options Converted Array is:");
            System.out.println(finalOptionsArr.toString());
//, "-M", "2"
           // String[] finalOptionsArr = new String[]{"-C", "0.25", "-M", "2"};

            if (cls instanceof OptionHandler) {
                ((OptionHandler) cls).setOptions(finalOptionsArr);
            }

            Instances data = new ConverterUtils.DataSource(file.getInputStream()).getDataSet();
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            // Splitting data into training and testing sets
            data.randomize(new Random(1)); // Randomize the data with a seed for reproducibility
            int trainSize = (int) Math.round(data.numInstances() * 0.8);
            int testSize = data.numInstances() - trainSize;
            Instances train = new Instances(data, 0, trainSize);
            Instances test = new Instances(data, trainSize, testSize);

            cls.buildClassifier(data);

            // Evaluating classifier
            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(cls, test);

            String results = "Classifier trained successfully. \nEvaluation results:\n" +
                    "Accuracy: " + String.format("%.2f%%", eval.pctCorrect()) + "\n" +
                    "Precision: " + String.format("%.2f%%", eval.weightedPrecision() * 100) + "\n" +
                    "Recall: " + String.format("%.2f%%", eval.weightedRecall() * 100) + "\n" +
                    "F1 Score: " + String.format("%.2f%%", eval.weightedFMeasure() * 100) + "\n" +
                    "Summary: " + eval.toSummaryString();

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error training classifier: " + e.getMessage());
        }
    }

   /* @PostMapping("/train")
    public ResponseEntity<String> trainModel(@RequestBody TrainingRequest request){

    }
    private String validateTrainingRequest(TrainingRequest request){
        if(request == null){
            return "Empty request received.";
        }
        String classifier = request.getClassifier();
        if(classifier == null || classifier.isEmpty() || classifier.isBlank()){
            return "No Classifier provided.";
        }
        if(request.getFile().)
    }
*/
}
