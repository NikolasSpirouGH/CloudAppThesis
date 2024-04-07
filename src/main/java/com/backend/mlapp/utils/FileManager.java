package com.backend.mlapp.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
public class FileManager {

    private final FileStorageService fileStorageService;
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

    public String csvToArff(InputStream inputStream, String fileReference) {
        File tempInputFile = null;
        File tempOutputFile = null;
        try {
            // Create a temporary file for the input data
            tempInputFile = File.createTempFile("input", getFileExtension(fileReference));
            Files.copy(inputStream, tempInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Determine if the file is CSV or ARFF and load the data accordingly
            Instances data;
            if (getFileExtension(fileReference).equalsIgnoreCase(".arff")) {
                ArffLoader arffLoader = new ArffLoader();
                arffLoader.setSource(tempInputFile);
                data = arffLoader.getDataSet();
            } else {
                CSVLoader csvLoader = new CSVLoader();
                csvLoader.setSource(tempInputFile);
                data = csvLoader.getDataSet();
            }
            // Create a separate temporary file for the ARFF output
            tempOutputFile = File.createTempFile("output", ".arff");

            // Save the data in ARFF format to the output file
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(tempOutputFile);
            saver.writeBatch();

            // Return the path of the output file
            return tempOutputFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert file to ARFF format", e);
        } finally {
            // Clean up the temporary input file
            if (tempInputFile != null) {
                tempInputFile.delete();
            }
            // Optionally, delete the output file if it's no longer needed
            // Note: If you return the file path, you might want to keep the file until it's used
            // tempOutputFile.delete();
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndex);
    }

}


