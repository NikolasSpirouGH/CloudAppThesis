package com.cloud_ml_app_thesis.enumeration;


public enum UserRoleEnum {

    USER, DATASET_MANAGER, ALGORITHM_MANAGER, GROUP_LEADER, TRAINING_MODEL_MANAGER, ADMIN;

    public String getAuthority() {
        return name();
    }
}
