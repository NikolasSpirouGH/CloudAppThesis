package com.cloud_ml_app_thesis.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class MinioConfig {

    @Value("http://127.0.0.1:9000")
    private String url;

    @Value("minioadmin")
    private String accessKey;

    @Value("minioadmin")
    private String secretKey;

    @Value("ml-datasets")
    private String datasetsBucketName;

    @Value("ml-models")
    private String modelsBucketName;

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(accessKey, secretKey)
                    .build();

            // Check if datasets bucket exists, if not create one
            ensureBucketExists(minioClient, datasetsBucketName);

            // Check if models bucket exists, if not create one
            ensureBucketExists(minioClient, modelsBucketName);

            return minioClient;
        } catch (MinioException e) {
            throw new RuntimeException("Error initializing Minio: " + e.getMessage());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureBucketExists(MinioClient minioClient, String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            System.out.println("Bucket '" + bucketName + "' created successfully.");
        } else {
            System.out.println("Bucket '" + bucketName + "' already exists.");
        }
    }
}