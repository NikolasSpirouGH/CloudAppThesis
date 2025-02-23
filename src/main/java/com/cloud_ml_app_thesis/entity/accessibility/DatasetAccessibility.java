package com.cloud_ml_app_thesis.entity.accessibility;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class DatasetAccessibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private com.cloud_ml_app_thesis.enumeration.accessibility.DatasetAccessibility accessibility;

    @Column
    private String description;
}
