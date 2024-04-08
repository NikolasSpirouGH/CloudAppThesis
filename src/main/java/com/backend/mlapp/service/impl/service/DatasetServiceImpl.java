package com.backend.mlapp.service.impl.service;

import com.backend.mlapp.entity.AppUser;
import com.backend.mlapp.entity.Dataset;
import com.backend.mlapp.exception.FileProcessingException;
import com.backend.mlapp.repository.DatasetRepository;
import com.backend.mlapp.service.DatasetService;
import com.backend.mlapp.service.UserService;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.minio.*;

import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DatasetServiceImpl implements DatasetService {

    private final MinioClient minioClient;

    private final DatasetRepository datasetRepository;

    private final UserService userService;

    private final String datasetsBucket = "dataset-files";

    @Override
    public String uploadFile(Integer userId, MultipartFile file) {
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
            String fileUrl = "http://127.0.0.1:9001" + "/" + datasetsBucket + "/" + fileName;

            Dataset uploadedFile = new Dataset();
            uploadedFile.setFileName(fileName);
            uploadedFile.setUrlPath(fileUrl);
            Optional<AppUser> user = userService.getUserById(userId);
            uploadedFile.setUser(user.get());
            datasetRepository.save(uploadedFile);
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
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