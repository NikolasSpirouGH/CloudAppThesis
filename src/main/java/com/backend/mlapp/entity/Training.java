package com.backend.mlapp.entity;


import com.backend.mlapp.enumeration.TrainingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_id")
    private Integer id;

    @Column
    private String trainingColumns;

    @Column
    private String targetColumn;

    @Column
    private LocalDate startedAt;

    @Column
    private LocalDate finishedAt;

    @Column
    @Enumerated(EnumType.STRING)
    private TrainingStatus status;

    @Column
    private String algorithmParam;

    @ManyToOne
    @JoinColumn(name="algorithm_id")
    private Algorithm algorithm;;
}
