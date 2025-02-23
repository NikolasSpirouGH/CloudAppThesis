package com.cloud_ml_app_thesis.service;

import com.cloud_ml_app_thesis.exception.FileProcessingException;
import com.cloud_ml_app_thesis.exception.MinioFileUploadException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public MinioService(@Value("${minio.url}") String minioUrl,
                        @Value("${minio.access.name}") String accessKey,
                        @Value("${minio.access.secret}") String secretKey) {
       /* if (!minioUrl.startsWith("http://") && !minioUrl.startsWith("https://")) {
            minioUrl = "http://" + minioUrl;
        }*/
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void uploadFile(MultipartFile file, String objectName) throws MinioFileUploadException {
        try {
            logger.info("Uploading file '{}' as '{}'", file.getOriginalFilename(), objectName);

            // Create the PutObjectArgs with the necessary details
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            // Upload the file to the specified bucket and object name
            minioClient.putObject(putObjectArgs);

            logger.info("Successfully uploaded file '{}' as '{}'", file.getOriginalFilename(), objectName);
        } catch (MinioException e) {
            logger.error("Error occurred while uploading the file to Minio", e);
            throw new MinioFileUploadException("Error occurred while uploading the file to Minio", e);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            logger.error("Technical error occurred while uploading the file", e);
            throw new RuntimeException("Technical error occurred while uploading the file", e);
        }
    }

    public InputStream getFileInputStream(String bucketName, String fileReference) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileReference)
                            .build()
            );
        } catch (Exception e) {
            throw new FileProcessingException("Error fetching file from MinIO: " + e.getMessage(), e);
        }
    }
}
