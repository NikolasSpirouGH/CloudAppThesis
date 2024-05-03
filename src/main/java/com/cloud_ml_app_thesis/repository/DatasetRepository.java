package com.cloud_ml_app_thesis.repository;
        
import com.cloud_ml_app_thesis
.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetRepository extends JpaRepository<Dataset, Integer> {
    Optional<Dataset> findByFileUrl(String fileUrl);
}
