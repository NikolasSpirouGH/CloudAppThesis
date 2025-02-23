package com.cloud_ml_app_thesis.repository;

import com.cloud_ml_app_thesis.entity.Model;
import com.cloud_ml_app_thesis.entity.Training;
import com.cloud_ml_app_thesis.enumeration.status.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Training, Integer> {
    Optional<List<Training>> findAllByOrderByStatusAsc();

    Optional<List<Training>> findAllByUserUsernameAndStatus(String username, TrainingStatus status);
    Optional<List<Training>> findAllByUserUsernameOrderByFinishedDateDesc(String username);


    Optional<List<Training>> findAllByUserIdAndStatus(Integer userId, TrainingStatus status);
    Optional<List<Training>> findAllByUserId(Integer userId);

    Optional<Training> findByModel(Model model);

    long countByDatasetConfigurationDatasetId(Integer datasetId);
    long countByDatasetConfigurationDatasetIdAndStatus(Integer datasetId, TrainingStatus status);

    long countByDatasetConfigurationIdAndStatus(Integer datasetConfigurationId, TrainingStatus status);


}
