package com.cloud_ml_app_thesis.entity.accessibility;

import com.cloud_ml_app_thesis.entity.Dataset;
import com.cloud_ml_app_thesis.enumeration.accessibility.DatasetAccessibilityEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="dataset_accessibility")
public class DatasetAccessibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @Enumerated(EnumType.STRING)
    private DatasetAccessibilityEnum accessibility;

    @Column
    private String description;

    @OneToOne(mappedBy = "accessibility")
    Dataset dataset;
}
