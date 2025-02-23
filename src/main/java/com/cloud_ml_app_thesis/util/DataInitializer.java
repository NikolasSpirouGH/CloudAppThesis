
package com.cloud_ml_app_thesis.util;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.enumeration.UserRole;
import com.cloud_ml_app_thesis.enumeration.status.UserStatus;
import com.cloud_ml_app_thesis.repository.AlgorithmRepository;
import com.cloud_ml_app_thesis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Option;
import weka.core.OptionHandler;

import java.lang.reflect.Method;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AlgorithmRepository algorithmRepository;

    private final String adminPassword = "adminPassword"; // Replace with actual password retrieval
    private final String userPassword = "userPassword"; // Replace with actual password retrieval

    @Override
    public void run(String... args) {
        recreateAdmins();
        initializeAlgorithms();
    }

    private void recreateAdmins() {
        List<User> admins = List.of(
                new User(null, "bigspy","nikolas", "Spirou", "nikolas@gmail.com", adminPassword, 27, "Senior SWE", "Greece",  Set.of(new Role(1, UserRole.USER, "Standard User", null)), new com.cloud_ml_app_thesis.entity.status.UserStatus(), null, null, null),
                new User(null, "nickriz", "Nikos", "Rizogiannis", "rizo@gmail.com", adminPassword, 27, "Senior SWE", "Greece",Set.of(new Role(1, UserRole.USER, "Standard User", null)), new com.cloud_ml_app_thesis.entity.status.UserStatus(), null, null, null),
                new User(null, "johnken","john", "kennedy", "john@gmail.com", userPassword, 27, "Senior SWE", "Greece", Set.of(new Role(1, UserRole.USER, "Standard User", null)), new com.cloud_ml_app_thesis.entity.status.UserStatus(), null, null, null)
        );

        admins.forEach(admin -> {
            userRepository.findByEmail(admin.getEmail())
                    .ifPresent(userRepository::delete);
            userRepository.save(admin);
        });

        System.out.println("Admins recreated.");
    }

    private void initializeAlgorithms() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("weka.classifiers"))
                .addUrls(ClasspathHelper.forPackage("weka.clusterers"))
                .setScanners(new SubTypesScanner()));

        Set<Class<? extends Classifier>> classifierClasses = reflections.getSubTypesOf(Classifier.class);
        Set<Class<? extends Clusterer>> clustererClasses = reflections.getSubTypesOf(Clusterer.class);

        List<Algorithm> algorithmInfos = new ArrayList<>();

        processAlgorithmClasses(classifierClasses, algorithmInfos, Classifier.class);
        processAlgorithmClasses(clustererClasses, algorithmInfos, Clusterer.class);

        saveAlgorithms(algorithmInfos);
    }

    private <T> void processAlgorithmClasses(Set<Class<? extends T>> classes, List<Algorithm> algorithmInfos, Class<T> type) {
        for (Class<? extends T> cls : classes) {
            try {
                // Skip abstract classes, interfaces, or inner classes
                if (java.lang.reflect.Modifier.isAbstract(cls.getModifiers()) || cls.isInterface() || cls.isMemberClass()) {
                    continue;
                }

                T instance = cls.getDeclaredConstructor().newInstance();
                String className = cls.getSimpleName();
                // Retrieve the package path relative to "weka.classifiers" or other packages
                Package pkg = cls.getPackage();
                String packageName = pkg.getName();
                String relativePath = packageName.substring(packageName.indexOf("weka.") + "weka.".length());

                // Check if globalInfo method is available
                Method globalInfoMethod = null;
                try {
                    globalInfoMethod = cls.getMethod("globalInfo");
                } catch (NoSuchMethodException e) {
                    // globalInfo method is not available
                }

                String classInfo = "(no description available)";
                if (globalInfoMethod != null) {
                    classInfo = (String) globalInfoMethod.invoke(instance);
                }

                String[] optionsDefaultArr = new String[]{};

                // Listing options if the algorithm implements OptionHandler
                if (instance instanceof OptionHandler) {
                    OptionHandler optionHandler = (OptionHandler) instance;
                    Enumeration<Option> options = optionHandler.listOptions();
                    StringBuilder optionsStr = new StringBuilder();
                    StringBuilder optionsDescrStr = new StringBuilder();
                    while (options.hasMoreElements()) {
                        Option option = options.nextElement();
                        if (option.name() != null && !option.name().isBlank()) {
                            optionsStr.append(option.name()).append(",");
                            optionsDescrStr.append(option.description()).append("->");
                        }
                        optionsDefaultArr = optionHandler.getOptions();
                    }
                    if (!optionsStr.isEmpty()) {
                        optionsStr.deleteCharAt(optionsStr.length() - 1); // Remove trailing comma
                    }
                    if (optionsDescrStr.length() > 1) {
                        optionsDescrStr.delete(optionsDescrStr.length() - 2, optionsDescrStr.length()); // Remove trailing "->"
                    }

                    String defaultOptionsFinal = Arrays.toString(optionsDefaultArr)
                            .replace("--, ", "")
                            .replace("[--, ", "")
                            .replace("--]", "")
                            .replaceAll("[\\[\\]]", ""); // Clean brackets

                    Algorithm algorithm = new Algorithm(null, className, classInfo.replaceAll("[\\t\\n]", ""),
                            optionsStr.toString().replaceAll("[\\t\\n]", "").replace(",,", ","),
                            optionsDescrStr.toString().replaceAll("[\\t\\n]", "").replace("->->", "->"),
                            defaultOptionsFinal, cls.getName());
                    algorithmInfos.add(algorithm);
                }
            } catch (Exception e) {
                System.err.println("Error processing class " + cls.getName() + ": " + e.getMessage());
            }
        }
    }

    private void saveAlgorithms(List<Algorithm> algorithmInfos) {
        Algorithm al = null;
        try {
            for (Algorithm a : algorithmInfos) {
                al = a;
                algorithmRepository.save(a);
            }
        } catch (DataIntegrityViolationException e) {
            System.out.println("Failed to save algorithm with name: " + (al != null ? al.getName() : "unknown") +
                    ", Description length: " + (al != null ? al.getDescription().length() : "unknown"));
            System.out.println("Options Description length: " + (al != null ? al.getOptionsDescription().length() : "unknown"));
        }
    }
}
