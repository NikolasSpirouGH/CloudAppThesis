package com.cloud_ml_app_thesis.util;

public class ValidationUtil {
    public static boolean stringExists(String str){
        return str == null || str.isEmpty() || str.isBlank();
    }
}
