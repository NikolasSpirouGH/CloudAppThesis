package com.backend.mlapp.repository;

import com.backend.mlapp.entity.Algorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlgorithmRepository extends JpaRepository<Algorithm, Integer> {
    Optional<Algorithm> findByName(String name);
}
