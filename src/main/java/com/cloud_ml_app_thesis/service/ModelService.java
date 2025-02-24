package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.Model;
import com.cloud_ml_app_thesis.entity.status.ModelStatus;
import com.cloud_ml_app_thesis.repository.ModelRepository;
import com.cloud_ml_app_thesis.repository.TrainRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.*;
import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final MinioClient minioClient;

    private final ModelRepository modelRepository;

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    private final TrainRepository trainRepository;

    @Value("${minio.url}")
    private String minioUrl;

    public String saveModelToMinio(String bucketName, String objectName, byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(
                                bais, data.length, -1)
                        .build());
        return minioUrl + "/" + bucketName + "/" + objectName;
    }

    public byte[] serializeModel(Object model) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(model);
            return bos.toByteArray();
        }
    }

    public void saveModel(Integer trainingId, String modelUrl, String results, String modelType) {
        Model model = new Model();
        model.setTraining(trainRepository.findById(trainingId).get());
        model.setUrlModelMinio(modelUrl); // Truncate if needed
        model.setEvaluation(results); // Truncate if needed
        model.setStatus(new ModelStatus());
        model.setModelType(modelType);
        modelRepository.save(model);
    }


    public String evaluateClassifier(Classifier cls, Instances train, Instances test) throws Exception {
        Evaluation eval = new Evaluation(train);
        eval.evaluateModel(cls, test);

        String results = "Classifier trained successfully. \nEvaluation results:\n" +
                "Accuracy: " + String.format("%.2f%%", eval.pctCorrect()) + "\n" +
                "Precision: " + String.format("%.2f%%", eval.weightedPrecision() * 100) + "\n" +
                "Recall: " + String.format("%.2f%%", eval.weightedRecall() * 100) + "\n" +
                "F1 Score: " + String.format("%.2f%%", eval.weightedFMeasure() * 100) + "\n" +
                "Summary: " + eval.toSummaryString();

        logger.info(results);
        return results;
    }

    public String evaluateClusterer(Clusterer clusterer, Instances data) throws Exception {
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
        eval.evaluateClusterer(data);

        String results = "Clusterer trained successfully. \nEvaluation results:\n" +
                eval.clusterResultsToString();

        logger.info(results);
        return results;
    }

    public Object loadModel(Integer modelId) throws Exception {
        logger.info("Loading model with ID: {}", modelId);

        Model modelEntity = modelRepository.findById(modelId)
                .orElseThrow(() -> {
                    String errorMessage = "Model not found with id: " + modelId;
                    logger.error(errorMessage);
                    return new RuntimeException(errorMessage);
                });

        URI modelUri = new URI(modelEntity.getUrlModelMinio());
        logger.info("Model URI: {}", modelUri);

        // Splitting the path to get bucket and object names
        String[] pathParts = modelUri.getPath().split("/");
        logger.info("Path parts: {}", (Object) pathParts);

        if (pathParts.length < 2) {
            throw new RuntimeException("Invalid model URI: " + modelUri);
        }

        String minioUrl = pathParts[0];
        String bucketName = pathParts[1];
        String objectName = String.join("/", Arrays.copyOfRange(pathParts, 2, pathParts.length));
        logger.info("Minio URL: {}", minioUrl);
        logger.info("Bucket Name: {}", bucketName);
        logger.info("Object Name: {}", objectName);

        try (InputStream modelStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            Object model = SerializationHelper.read(modelStream);
            logger.info("Model loaded successfully");


            if ("classifier".equalsIgnoreCase(modelEntity.getModelType()) && model instanceof Classifier) {
                return model;
            } else if ("clusterer".equalsIgnoreCase(modelEntity.getModelType()) && model instanceof Clusterer) {
                return model;
            } else {
                String errorMessage = "Model type mismatch or unsupported model type: " + modelEntity.getModelType();
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (MinioException e) {
            String errorMessage = "Error loading model from Minio: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while loading model: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


}

