package com.cloud_ml_app_thesis.repository.status;

import com.cloud_ml_app_thesis.entity.status.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingStatusRepository extends JpaRepository<TrainingStatus, Integer> {
    Optional<TrainingStatus> findByName(com.cloud_ml_app_thesis.enumeration.status.TrainingStatus name);
}

