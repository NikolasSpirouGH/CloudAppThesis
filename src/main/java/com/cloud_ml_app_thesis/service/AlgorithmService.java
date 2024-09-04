package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.entity.AlgorithmConfiguration;
import com.cloud_ml_app_thesis.repository.AlgorithmConfigurationRepository;
import com.cloud_ml_app_thesis.repository.AlgorithmRepository;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.clusterers.Clusterer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AlgorithmService {

    private final AlgorithmRepository algorithmRepository;
    private final AlgorithmConfigurationRepository algorithmConfigurationRepository;
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmService.class);


    public Map<String, String> getWekaAlgorithms() {
        Map<String, String> wekaAlgoInfos = new HashMap<>();

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("weka.classifiers"))
                .setScanners(new SubTypesScanner()));

        Set<Class<? extends Classifier>> classes = reflections.getSubTypesOf(Classifier.class);

        for (Class<? extends Classifier> cls : classes) {
            try {
                Classifier instance = cls.getDeclaredConstructor().newInstance();
                String classifierName = cls.getSimpleName();

                // Retrieve the package path relative to "weka.classifiers"
                Package pkg = cls.getPackage();
                String packageName = pkg.getName();
                String relativePath = packageName.substring(packageName.indexOf("weka.classifiers") + "weka.classifiers".length());

                System.out.println("Package path: " + relativePath);

                // Check if globalInfo method is available
                Method globalInfoMethod = null;
                try {
                    globalInfoMethod = cls.getMethod("globalInfo");
                } catch (NoSuchMethodException e) {
                    System.out.println("No globalInfo() method for " + classifierName);
                }

                if (globalInfoMethod != null) {
                    String classifierInfo = (String) globalInfoMethod.invoke(instance);
                    System.out.println("Name: " + classifierName);
                    System.out.println("Description: " + classifierInfo);
                    wekaAlgoInfos.put(classifierName, classifierInfo);

                } else {
                    System.out.println("Name: " + classifierName + " (no description available)");
                    wekaAlgoInfos.put(classifierName, "(no description available)");
                }

                // Listing options if the classifier implements OptionHandler
                if (instance instanceof OptionHandler) {
                    OptionHandler optionHandler = (OptionHandler) instance;
                    Enumeration<Option> options = optionHandler.listOptions();
                    System.out.println("Options:");
                    while (options.hasMoreElements()) {
                        Option option = options.nextElement();
                        System.out.println("\t- " + option.synopsis() + " " + option.description());
                        wekaAlgoInfos.put(option.name(), option.description());
                    }
                }
                System.out.println("---");
            } catch (Exception e) {
                System.err.println("Error processing class " + cls.getName() + ": " + e.getMessage());
            }
        }
        return wekaAlgoInfos;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithmRepository.findAll();
    }

    public void chooseAlgorithm(Integer id, String options) {
        Optional<Algorithm> algorithm = algorithmRepository.findById(id);
        AlgorithmConfiguration algorithmConfiguration = new AlgorithmConfiguration();
        algorithmConfiguration.setOptions(options);
        algorithmConfiguration.setAlgorithm(algorithm.get());

        algorithmConfigurationRepository.save(algorithmConfiguration);
    }



    public AlgorithmConfiguration getAlgorithmConfiguration(Integer algoConfId) throws Exception {
        return algorithmConfigurationRepository.findById(algoConfId)
                .orElseThrow(() -> new Exception("Algorithm configuration ID " + algoConfId + " not found."));
    }



}
