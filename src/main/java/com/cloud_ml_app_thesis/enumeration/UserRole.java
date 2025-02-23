package com.cloud_ml_app_thesis.enumeration;


public enum UserRole {

    USER, GROUP_LEADER, TRAINING_MODEL_MANAGER, DATASET_MANAGER, ALGORITHM_MANAGER, ADMIN;

    public String getAuthority() {
        return name();
    }
}
