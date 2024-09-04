package com.cloud_ml_app_thesis.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtil {

    public static String generateUniqueFilename(String originalFilename, String username){
        String timestamp = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss").format(LocalDateTime.now());
        String[] filenameArr = originalFilename.split("\\.");
        String uniqueFilename = filenameArr[0].replaceAll("\\s+", "_").concat("_").concat(timestamp);

        return  uniqueFilename + "_" + username + "." + filenameArr[1];
    }
}
