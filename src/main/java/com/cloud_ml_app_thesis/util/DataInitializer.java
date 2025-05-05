
package com.cloud_ml_app_thesis.util;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.cloud_ml_app_thesis.entity.Category;
import com.cloud_ml_app_thesis.entity.Role;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.accessibility.DatasetAccessibility;
import com.cloud_ml_app_thesis.entity.status.ModelStatus;
import com.cloud_ml_app_thesis.entity.status.TrainingStatus;
import com.cloud_ml_app_thesis.entity.status.UserStatus;
import com.cloud_ml_app_thesis.enumeration.UserRoleEnum;
import com.cloud_ml_app_thesis.enumeration.accessibility.DatasetAccessibilityEnum;
import com.cloud_ml_app_thesis.enumeration.status.ModelStatusEnum;
import com.cloud_ml_app_thesis.enumeration.status.TrainingStatusEnum;
import com.cloud_ml_app_thesis.enumeration.status.UserStatusEnum;
import com.cloud_ml_app_thesis.repository.*;
import com.cloud_ml_app_thesis.repository.accessibility.DatasetAccessibilityRepository;
import com.cloud_ml_app_thesis.repository.status.ModelStatusRepository;
import com.cloud_ml_app_thesis.repository.status.TrainingStatusRepository;
import com.cloud_ml_app_thesis.repository.status.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
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
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final AlgorithmRepository algorithmRepository;
    private final CategoryRepository categoryRepository;
    private final TrainingStatusRepository trainingStatusRepository;
    private final ModelStatusRepository modelStatusRepository;
    private final DatasetAccessibilityRepository datasetAccessibilityRepository;

    private final String adminPassword = "adminPassword"; // Replace with actual password retrieval
    private final String userPassword = "userPassword"; // Replace with actual password retrieval
    private final Argon2PasswordEncoder passwordEncoder;
    private final ModelExecutionRepository modelExecutionRepository;

    @Override
    public void run(String... args) {
        initializeUserStatuses();
        initializeTrainingStatuses();
        initializeModelStatuses();
        initializeDatasetAccessibility();
        initializeAlgorithms();
        initializeUserRoles();
        recreateAdmins();
        initializeCategories();

    }

    private void initializeCategories(){
        Category category = new Category();
        category.setName("Uncategorized");
        category.setDescription("Category for entities that have no parent category.");
        category.setCreatedBy(userRepository.findByUsername("bigspy").orElseThrow());
        categoryRepository.save(category);

    }

    private void initializeUserRoles(){
        if (roleRepository.count() == 0) {
            List<Role> userRolesList = new ArrayList<>();
            for(int i=0; i< UserRoleEnum.values().length; i++){
                userRolesList.add(new Role(null, UserRoleEnum.values()[i], "Some description", null));
            }
            roleRepository.saveAll(userRolesList);
        }
    }
    private void initializeUserStatuses(){
        if (userStatusRepository.count() == 0) {
            List<UserStatus> userStatusesList = new ArrayList<>();
            for(int i=0; i< UserStatusEnum.values().length; i++){
                userStatusesList.add(new UserStatus(null, UserStatusEnum.values()[i], "Some description"));
            }
            userStatusRepository.saveAll(userStatusesList);
        }
    }
    private void initializeTrainingStatuses(){
        if (trainingStatusRepository.count() == 0) {
            List<TrainingStatus> trainingStatusesList = new ArrayList<>();
            for(int i=0; i< TrainingStatusEnum.values().length; i++){
                trainingStatusesList.add(new TrainingStatus(null, TrainingStatusEnum.values()[i], "Some description"));
            }
            trainingStatusRepository.saveAll(trainingStatusesList);
        }
    }
    private void initializeModelStatuses(){
        if (modelStatusRepository.count() == 0) {
            List<ModelStatus> modelStatusesList = new ArrayList<>();
            for(int i=0; i< ModelStatusEnum.values().length; i++){
                modelStatusesList.add(new ModelStatus(null, ModelStatusEnum.values()[i], "Some description"));
            }
            modelStatusRepository.saveAll(modelStatusesList);
        }
    }
    private void initializeDatasetAccessibility(){
        if (datasetAccessibilityRepository.count() == 0) {
            List<DatasetAccessibility> datasetAccessibilityList = new ArrayList<>();
            for(int i = 0; i< DatasetAccessibilityEnum.values().length; i++){
                datasetAccessibilityList.add(new DatasetAccessibility(null, DatasetAccessibilityEnum.values()[i], "Some description"));
            }
            datasetAccessibilityRepository.saveAll(datasetAccessibilityList);
        }
    }
    private void recreateAdmins() {
        UserStatus defaultStatus = userStatusRepository.findByName(UserStatusEnum.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Default status not found"));
        Role userRole = roleRepository.findByName(UserRoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("Role USER was not found"));
        Role adminRole = roleRepository.findByName(UserRoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("Role USER was not found"));
        List<User> admins = List.of(
                new User(null, "bigspy","nikolas", "Spirou", "nikolas@gmail.com", passwordEncoder.encode(adminPassword), 27, "Senior SWE", "Greece",  Set.of(userRole),defaultStatus, null, null, null),
                new User(null, "nickriz", "Nikos", "Rizogiannis", "rizo@gmail.com", passwordEncoder.encode(adminPassword), 27, "Senior SWE", "Greece",Set.of(userRole), defaultStatus, null, null, null),
                new User(null, "johnken","john", "kennedy", "john@gmail.com", passwordEncoder.encode(userPassword), 27, "Senior SWE", "Greece", Set.of(adminRole), defaultStatus, null, null, null)
        );
        modelExecutionRepository.deleteAll();
        categoryRepository.deleteAll();
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
