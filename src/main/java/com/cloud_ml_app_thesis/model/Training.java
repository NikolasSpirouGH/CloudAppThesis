package com.cloud_ml_app_thesis.model;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis
.model.TrainingStatus status;

    @ManyToOne
    @JoinColumn(name="id")
    private com.cloud_ml_app_thesis
.model.AlgorithmConfiguration algorithmConfiguration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private com.cloud_ml_app_thesis
.model.AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    private com.cloud_ml_app_thesis
.model.DatasetConfiguration datasetConfiguration;

}
