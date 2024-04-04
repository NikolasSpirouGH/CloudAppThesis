package com.backend.mlapp.config;


import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileManager {
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public Instances loadDataset(String arffFile, Integer targetClassCol) throws Exception {
        try {
            System.out.println("Loading dataset from ARFF file: " + arffFile);
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(arffFile);
            Instances dataset = source.getDataSet();
            if (dataset == null) {
                throw new IOException("Failed to load dataset from ARFF file: " + arffFile);
            }
            Attribute targetClassAttribute = dataset.attribute(dataset.numAttributes() - 1);
            String targetClassName = targetClassAttribute.name();
            if (targetClassCol == null || targetClassCol < 0 || targetClassCol >= dataset.numAttributes()) {
                targetClassCol = dataset.numAttributes() - 1;
            }
            dataset.setClassIndex(targetClassCol);
            return dataset;
        } catch (Exception e) {
            System.err.println("Error loading dataset from ARFF file: " + arffFile);
            e.printStackTrace();
            throw e;
        }
    }

    public String csvToArff(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        System.out.println("Original filename: " + originalFilename);
        File tempFile = File.createTempFile("temp", ".csv");
        file.transferTo(tempFile);
        ArffLoader arrfLoader = new ArffLoader();
        CSVLoader csvLoader = new CSVLoader();
        Instances data;
        if (originalFilename != null && originalFilename.endsWith(".arff")) {
            arrfLoader.setSource(tempFile);
            data = arrfLoader.getDataSet();
        } else {
            csvLoader.setSource(tempFile);
            data = csvLoader.getDataSet();
        }
        String outputFile = TEMP_DIR + File.separator + UUID.randomUUID() + ".arff";
        System.out.println("Output ARFF file path: " + outputFile);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(outputFile));
        saver.writeBatch();

        tempFile.delete();

        return outputFile;
    }
}


