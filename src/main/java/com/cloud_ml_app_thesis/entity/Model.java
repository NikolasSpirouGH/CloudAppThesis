package com.cloud_ml_app_thesis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "models")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "training_id")
    private Training training;

    @Column(name = "url_model_minio", length = 1000)
    private String urlModelMinio;

    @Column(name = "evaluation", length = 5000)
    private String evaluation;

    @Column(name = "status")
    private String status;

    @Column(name = "model_type")
    private String modelType; // e.g., "classifier" or "clusterer"
}