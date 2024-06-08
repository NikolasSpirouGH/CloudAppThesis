package com.cloud_ml_app_thesis.repository;

import com.cloud_ml_app_thesis.entity.Training;
import com.cloud_ml_app_thesis.enumeration.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Training, Integer> {
    List<Training> findAllByOrderByStatusAsc();
}
