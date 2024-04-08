package com.backend.mlapp.entity;


import com.backend.mlapp.enumeration.TrainingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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
    @JoinColumn(name="id")
    private Algorithm algorithm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Dataset dataset;

}
