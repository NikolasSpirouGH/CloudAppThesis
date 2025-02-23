package com.cloud_ml_app_thesis.repository.accessibility;

import com.cloud_ml_app_thesis.entity.accessibility.ModelAccessibility;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ModelAccessibilityRepository extends JpaRepository<ModelAccessibility, Integer> {
}

