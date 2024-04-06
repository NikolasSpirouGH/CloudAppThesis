package com.backend.mlapp.payload;

import com.backend.mlapp.entity.AppUser;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TrainRequest {

    private MultipartFile file;

    @NotBlank
    private String algorithm;

    private String algorithmConfigs;

    private Integer targetClassCol;

    private int folds;
}
