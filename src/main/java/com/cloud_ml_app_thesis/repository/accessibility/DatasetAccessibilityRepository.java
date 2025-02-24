package com.cloud_ml_app_thesis.repository.accessibility;

import com.cloud_ml_app_thesis.entity.accessibility.DatasetAccessibility;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DatasetAccessibilityRepository extends JpaRepository<DatasetAccessibility, Integer> {
}
