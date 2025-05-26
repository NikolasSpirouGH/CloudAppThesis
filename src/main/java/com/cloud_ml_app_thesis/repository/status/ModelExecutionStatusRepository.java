package com.cloud_ml_app_thesis.repository.status;

import com.cloud_ml_app_thesis.entity.status.ModelExecutionStatus;
import com.cloud_ml_app_thesis.enumeration.status.ModelExecutionStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelExecutionStatusRepository extends JpaRepository<ModelExecutionStatus, Integer> {
    Optional<ModelExecutionStatus> findByName(ModelExecutionStatusEnum name);
}
