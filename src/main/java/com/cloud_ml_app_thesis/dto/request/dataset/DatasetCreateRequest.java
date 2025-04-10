package com.cloud_ml_app_thesis.dto.request.dataset;

import com.cloud_ml_app_thesis.entity.Category;
import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.entity.accessibility.DatasetAccessibility;
import com.cloud_ml_app_thesis.enumeration.accessibility.DatasetAccessibilityEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Getter
public class DatasetCreateRequest {

    @Size(max = 20, message = "Username cannot exceed 20 characters")
    @NotBlank(message = "You must provide an User owner id")
    private String ownerUsername;

    @NotNull(message = "File must be provided")
    private MultipartFile file;

    @Size(max = 50, message = "Original file name  must be at most 50 characters")
    private String originalFileName;

    @Size(max = 50, message = "File name must be at most 50 characters")
    private String fileName;

    @Size(max = 50, message = "File path must be at most 50 characters")
    private String filePath;

    @NotBlank(message = "To create dataset you must provide accessibility type")
    private DatasetAccessibilityEnum accessibility;

    private Set<@Size(min = 36, max = 36, message = "User id must be 36 characters")UUID> sharedUserIds;

    @Size(max = 5000, message = "Description cannot exceed 500 characters")
    @NotBlank(message = "You have to provide a description")
    private String description;

    @Positive(message = "Id cannot be negative")
    private Integer categoryId;
}
