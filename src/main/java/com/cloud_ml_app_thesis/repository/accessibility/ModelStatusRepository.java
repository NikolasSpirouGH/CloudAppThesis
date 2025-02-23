package com.cloud_ml_app_thesis.repository.accessibility;

import com.cloud_ml_app_thesis.entity.status.ModelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ModelStatusRepository extends JpaRepository<ModelStatus, Integer> {
}
