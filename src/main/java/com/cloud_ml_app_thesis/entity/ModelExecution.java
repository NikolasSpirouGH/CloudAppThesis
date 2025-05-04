package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="models_executions")
@Setter
@Getter
public class ModelExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private Model model;

    private LocalDateTime executedAt;

    private boolean success;

    @Column(length = 5000)
    private String predictionResult;

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;
}
