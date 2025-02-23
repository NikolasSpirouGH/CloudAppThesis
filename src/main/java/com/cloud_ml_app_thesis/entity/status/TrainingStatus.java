package com.cloud_ml_app_thesis.entity.status;

import com.cloud_ml_app_thesis.entity.Training;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name= "training_status")
public class TrainingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis.enumeration.status.TrainingStatus name;

    @Column(name = "description", length = 1000)
    private String description;

    @OneToOne(mappedBy = "status")
    private Training training;
}
