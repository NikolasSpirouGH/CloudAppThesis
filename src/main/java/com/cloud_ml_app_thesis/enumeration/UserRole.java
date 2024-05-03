package com.cloud_ml_app_thesis.enumeration;


public enum UserRole {

    USER, ADMIN;

    public String getAuthority() {
        return name();
    }
}
