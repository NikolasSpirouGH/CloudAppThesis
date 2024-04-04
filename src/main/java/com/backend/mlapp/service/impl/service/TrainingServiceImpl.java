package com.backend.mlapp.service.impl.service;

import com.backend.mlapp.config.FileManager;
import com.backend.mlapp.entity.Algorithm;
import com.backend.mlapp.entity.Training;
import com.backend.mlapp.enumeration.TrainingStatus;
import com.backend.mlapp.exception.*;
import com.backend.mlapp.payload.TrainRequest;
import com.backend.mlapp.repository.AlgorithmRepository;
import com.backend.mlapp.repository.TrainingRepository;
import com.backend.mlapp.service.TrainingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final ObjectMapper objectMapper;

    private final TrainingRepository trainingRepository;

    private final AlgorithmRepository algorithmRepository;

    private final FileManager fileManager;
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private static final Map<String, Map<String, String>> defaultAlgorithmParams = new HashMap<>();

    static {
        Map<String, String> j48Defaults = new HashMap<>();
        j48Defaults.put("C", "0.25"); // Confidence factor for pruning.
        j48Defaults.put("M", "2"); // Minimum number of instances per leaf.
        defaultAlgorithmParams.put("j48", j48Defaults);

        Map<String, String> svmDefaults = new HashMap<>();
        svmDefaults.put("C", "1.0");
        svmDefaults.put("K", "weka.classifiers.functions.supportVector.PolyKernel -E 1.0"); // Polynomial kernel with exponent 1.0
        defaultAlgorithmParams.put("svm", svmDefaults);

        Map<String, String> naiveBayesDefaults = new HashMap<>();
        // Naive Bayes doesn't need
        defaultAlgorithmParams.put("naive-bayes", naiveBayesDefaults);

        Map<String, String> kNNDefaults = new HashMap<>();
        kNNDefaults.put("K", "1"); // Number of nearest neighbors.
        defaultAlgorithmParams.put("k-nn", kNNDefaults);

        Map<String, String> simpleKMeansDefaults = new HashMap<>();
        simpleKMeansDefaults.put("N", "3");
        simpleKMeansDefaults.put("A", "weka.core.EuclideanDistance");
        simpleKMeansDefaults.put("I", "500");
        simpleKMeansDefaults.put("S", "10");
        defaultAlgorithmParams.put("skm", simpleKMeansDefaults);
    }

    @Override
    @Async
    public CompletableFuture<String> trainModel(TrainRequest trainRequest) {
        try {
            Algorithm algorithm = algorithmRepository.findByName(trainRequest.getAlgorithm())
                    .orElseThrow(() -> new ResourceNotFoundException("Algorithm does not exists."));
            Training training = new Training();
            training.setAlgorithmParam(trainRequest.getAlgorithmConfigs());
            training.setStatus(TrainingStatus.REQUESTED);
            training.setStartedAt(LocalDate.now());
            training.setTargetColumn(String.valueOf(trainRequest.getTargetClassCol()));
            training.setAlgorithm(algorithm);
            trainingRepository.save(training);

            String arffFile = fileManager.csvToArff(trainRequest.getFile());
            Instances dataset = fileManager.loadDataset(arffFile, trainRequest.getTargetClassCol());

            Object model = createModel(trainRequest.getAlgorithm());
            configureAndTrainModel(model, training, dataset, trainRequest);

            training.setStatus(TrainingStatus.COMPLETE);
            training.setFinishedAt(LocalDate.now());
            trainingRepository.save(training);
            return CompletableFuture.completedFuture(UUID.randomUUID().toString());

        } catch (Exception e) {
            logger.error("Failed to train model: {}", e.getMessage(), e);
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    private void configureAndTrainModel(Object model, Training training, Instances dataset, TrainRequest trainRequest) throws Exception {
        if (model instanceof Classifier classifier) {
            logger.info("Starting training model with ID: {}", training.getId());
            if (trainRequest.getAlgorithmConfigs() != null) {
                Map<String, String> algorithmConfigs = objectMapper.readValue(trainRequest.getAlgorithmConfigs(), new TypeReference<>() {
                });
                setClassifierParams(classifier, algorithmConfigs);
            }
            reportClassifierInfo(classifier, dataset, trainRequest);
            evaluate(classifier, dataset, trainRequest.getFolds());
        } else if (model instanceof SimpleKMeans kmeans) {
            logger.info("Starting cluster training model with ID: {}", training.getId());
            if (trainRequest.getAlgorithmConfigs() != null) {
                Map<String, String> algorithmConfigs = objectMapper.readValue(trainRequest.getAlgorithmConfigs(), new TypeReference<>() {});
                setClustererParams(kmeans, algorithmConfigs);
            }
            dataset.setClassIndex(-1);
            kmeans.buildClusterer(dataset);
            reportClusterInfo(kmeans, dataset);
        }
    }

    private static Object createModel(String algorithm) {
        return switch (algorithm.toLowerCase()) {
            case "naive-bayes" -> new NaiveBayes();
            case "k-nn" -> new IBk();
            case "j48" -> new J48();
            case "svm" -> new SMO();
            case "skm" -> new SimpleKMeans();
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };
    }

    private static void setAlgorithmOptions(Object algorithm, Map<String, String> params) throws Exception {
        if (!(algorithm instanceof weka.core.OptionHandler)) {
            throw new AlgorithmParameterSettingException(algorithm.getClass().getSimpleName() + " does not support option handling.");
        }

        weka.core.OptionHandler optionHandler = (weka.core.OptionHandler) algorithm;
        List<String> optionsList = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            optionsList.add("-" + entry.getKey());
            optionsList.add(entry.getValue());
        }
        try {
            optionHandler.setOptions(optionsList.toArray(new String[0]));
        } catch (Exception e) {
            throw new AlgorithmParameterSettingException("Error setting options for " + algorithm.getClass().getSimpleName(), e);
        }
    }

    private static Map<String, String> mergeParamsWithDefaults(Object algorithm, Map<String, String> algorithmParams) {
        String algorithmName = algorithm.getClass().getSimpleName().toLowerCase();
        Map<String, String> defaults = defaultAlgorithmParams.getOrDefault(algorithmName, new HashMap<>());
        Map<String, String> effectiveParams = new HashMap<>(defaults);
        if (algorithmParams != null) {
            effectiveParams.putAll(algorithmParams);
        }
        return effectiveParams;
    }

    private static void setClassifierParams(Classifier classifier, Map<String, String> algorithmParams) {
        Map<String, String> effectiveParams = mergeParamsWithDefaults(classifier, algorithmParams);
        try {
            setAlgorithmOptions(classifier, effectiveParams);
        } catch (Exception e) {
            throw new AlgorithmParameterSettingException("Failed to set parameters for classifier " + classifier.getClass().getSimpleName(), e);
        }
    }

    private static void setClustererParams(Clusterer clusterer, Map<String, String> algorithmParams) {
        Map<String, String> effectiveParams = mergeParamsWithDefaults(clusterer, algorithmParams);
        try {
            setAlgorithmOptions(clusterer, effectiveParams);
        } catch (Exception e) {
            throw new AlgorithmParameterSettingException("Failed to set parameters for clusterer " + clusterer.getClass().getSimpleName(), e);
        }
    }

    private void reportClassifierInfo(Classifier classifier, Instances dataset, TrainRequest trainRequest) throws Exception {
        Evaluation eval = new Evaluation(dataset);
        Random rand = new Random(1);
        int folds = trainRequest.getFolds();

        eval.crossValidateModel(classifier, dataset, folds, rand);

        logger.info("Classifier: {}", classifier.getClass().getSimpleName());
        logger.info("Dataset size: {} instances", dataset.numInstances());
        logger.info("Number of attributes: {}", dataset.numAttributes());
        logger.info("Class attribute: {}", dataset.classAttribute().name());
        logger.info(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
        logger.info(eval.toClassDetailsString("=== Detailed Accuracy By Class ==="));

        logger.info("=== Confusion Matrix ===");
        double[][] confusionMatrix = eval.confusionMatrix();
        for (double[] row : confusionMatrix) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < row.length; i++) {
                sb.append(String.format("%-7.0f", row[i]));
                if (i < row.length - 1) sb.append(", ");
            }
            sb.append("]");
            logger.info(sb.toString());
        }
    }

    private void reportClusterInfo(SimpleKMeans kmeans, Instances dataset) throws Exception {
        logger.info("Within-cluster sum of squared errors: {}", kmeans.getSquaredError());
        Instances centroids = kmeans.getClusterCentroids();
        for (int i = 0; i < centroids.numInstances(); i++) {
            logger.info("Cluster {} centroid: {}", i, centroids.instance(i));
        }
        double[] sizes = kmeans.getClusterSizes();
        for (int i = 0; i < sizes.length; i++) {
            logger.info("Cluster {} size: {}", i, sizes[i]);
        }
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(kmeans);
        eval.evaluateClusterer(dataset);
        logger.info("Cluster Evaluation Results:\n {}", eval.clusterResultsToString());
    }

    private void evaluate(Object model, Instances dataset, int folds) {
        try {
            if (model instanceof Classifier classifier) {
                evaluateClassifier(classifier, dataset, folds);
            } else if (model instanceof SimpleKMeans kmeans) {
                evaluateClusterer(kmeans, dataset);
            } else {
                throw new UnsupportedAlgorithmException("Unsupported algorithm type: " + model.getClass().getSimpleName());
            }
        } catch (Exception e) {
            throw new ModelEvaluationException("Failed to evaluate model", e);
        }
    }

    private void evaluateClassifier(Classifier classifier, Instances dataset, int folds) throws Exception {
        int seed = 1;
        Random rand = new Random(seed);
        Instances randData = new Instances(dataset);
        randData.randomize(rand);

        if (randData.classAttribute().isNominal())
            randData.stratify(folds);

        Evaluation eval = new Evaluation(randData);
        for (int n = 0; n < folds; n++) {
            Instances train = randData.trainCV(folds, n);
            Instances test = randData.testCV(folds, n);
            classifier.buildClassifier(train);
            eval.evaluateModel(classifier, test);
        }

        logger.info(eval.toSummaryString("=== Average Evaluation Results ===", false));
    }

    private void evaluateClusterer(SimpleKMeans kmeans, Instances dataset) throws Exception {
        kmeans.buildClusterer(dataset);
        logger.info("Within-cluster sum of squared errors: {}", kmeans.getSquaredError());
    }
}

/*@Override
public String saveModelToMinIO (String trainingId) throws IOException {
      *//*  // Retrieve your model based on trainingId. Implementation depends on your application.
        Classifier model = getModelByTrainingId(trainingId);

        // Serialize the model. The implementation might vary depending on how your models are represented.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(model);
        byte[] modelBytes = baos.toByteArray();

        // Define a unique name for the model file
        String modelName = "model-" + trainingId + ".model";

        // Use MinioClient to upload the model
        minioClient.putObject(
                PutObjectArgs.builder().bucket(minioBucketName).object(modelName)
                        .stream(new ByteArrayInputStream(modelBytes), modelBytes.length, -1)
                        .contentType("application/octet-stream")
                        .build());

        return modelName;*//*
    return null;
}*/



