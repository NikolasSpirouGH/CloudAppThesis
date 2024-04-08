package com.backend.mlapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface DatasetService {

    String uploadFile(Integer userId, MultipartFile file);

    InputStream getFileInputStream(String bucketName, String fileReference);
}
