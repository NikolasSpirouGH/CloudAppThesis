package com.backend.mlapp.utils;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;
import io.minio.*;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    private final String datasetsBucket = "dataset-files";

    public String uploadFile(MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(datasetsBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(datasetsBucket).build());
            }
            String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            InputStream is = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(datasetsBucket).object(fileName).stream(
                                    is, is.available(), -1)
                            .contentType(file.getContentType())
                            .build());
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    public InputStream getFileInputStream(String bucketName, String fileReference) throws Exception {
        try {
            // Fetch the file as an InputStream directly from MinIO
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileReference)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error fetching file from MinIO: " + e.getMessage(), e);
        }
    }
}