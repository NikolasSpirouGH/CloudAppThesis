package com.cloud_ml_app_thesis.repository;

import com.cloud_ml_app_thesis
.model.DatasetConfiguration;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DatasetConfigurationRepository extends JpaRepository<DatasetConfiguration, Integer> {

}
