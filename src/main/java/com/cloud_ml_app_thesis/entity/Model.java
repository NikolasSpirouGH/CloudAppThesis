package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.entity.accessibility.ModelAccessibility;
import com.cloud_ml_app_thesis.entity.status.ModelStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

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


    @Column(name = "type")
    private String modelType;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private ModelStatus status;

    @ManyToOne
    @JoinColumn(name = "accessibility_id")
    private ModelAccessibility accessibility;

    @ManyToMany
    @JoinTable(
            name = "model_categories",
            joinColumns = @JoinColumn(name ="model_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelExecution> executions;
}