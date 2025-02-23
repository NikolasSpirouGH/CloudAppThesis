package com.cloud_ml_app_thesis.entity.status;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name= "model_status")
public class ModelStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis.enumeration.status.ModelStatus name;
    @Column(name = "name", length = 1000)
    private String description;
}
