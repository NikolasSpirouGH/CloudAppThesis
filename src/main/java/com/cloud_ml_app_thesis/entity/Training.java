package com.cloud_ml_app_thesis.entity;


import com.cloud_ml_app_thesis.enumeration.TrainingStatus;
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
    private TrainingStatus status;

    @ManyToOne
    @JoinColumn(name="algorithm_id")
    private AlgorithmConfiguration algorithmConfiguration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="dataset_id")
    private DatasetConfiguration datasetConfiguration;

    @OneToOne(mappedBy = "training")
    private Model model;

    @Column(name = "results", length = 3000)
    private String results;

}
