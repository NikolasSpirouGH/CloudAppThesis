package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.entity.accessibility.ModelAccessibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    @Column(name = "type")
    private String modelType;
    @OneToOne
    @JoinColumn(name = "model_accessibility_id")
    private ModelAccessibility modelAccessibility;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelExecution> executions;
}