package com.cloud_ml_app_thesis.service;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.classifiers.Classifier;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

@Service
public class TrainService {



    public String getClassifierOptions(String classifierName){
        try {
            Classifier classifier = (Classifier) Class.forName("weka.classifiers." + classifierName).getDeclaredConstructor().newInstance();
            String defaultOptions[] = ((OptionHandler) classifier).getOptions();
            return Utils.joinOptions(defaultOptions);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


//        return null;
    }
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



    private List<Class<? extends Classifier>> findClasses() {
        List<Class<? extends Classifier>> classes = new ArrayList<>();
        // Assuming "weka.classifiers" is in a directory in the classpath
        // This is a simplistic way to gather classes; consider using a library like Reflections (org.reflections:reflections)
        String[] classifierNames = {
                "weka.classifiers.trees.J48",
                "weka.classifiers.bayes.NaiveBayes",
                // Add other classifier names here
        };

        for (String className : classifierNames) {
            try {
                Class<?> cls = Class.forName(className);
                if (Classifier.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
                    classes.add(cls.asSubclass(Classifier.class));
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + className);
            }
        }
        return classes;
    }


    public Classifier findClassifierByName(String classifierName) throws Exception {
        // Set up Reflections to scan the weka.classifiers package and its subpackages
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("weka.classifiers"))
                .setScanners(new SubTypesScanner()));

        // Get all subclasses of Classifier in the specified package
        Set<Class<? extends Classifier>> classes = reflections.getSubTypesOf(Classifier.class);

        // Search for the classifier by simple name
        for (Class<? extends Classifier> cls : classes) {
            if (cls.getSimpleName().equals(classifierName)) {
                System.out.println("Found classifier: " + cls.getName());

                return cls.getDeclaredConstructor().newInstance(); // Create a new instance
            }
        }

        throw new IllegalArgumentException("Classifier not found: " + classifierName);
    }
}
