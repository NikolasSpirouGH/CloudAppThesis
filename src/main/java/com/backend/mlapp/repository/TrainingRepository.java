package com.backend.mlapp.repository;


import com.backend.mlapp.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Integer> {

    @Query("SELECT t FROM Training t WHERE t.user.id = :userId AND (t.startedAt BETWEEN :startDate AND :endDate) AND (:algorithm IS NULL OR t.algorithm.name = :algorithm)")
    List<Training> findByUserIdAndCriteria(Integer userId, LocalDate startDate, LocalDate endDate, String algorithm);
}
