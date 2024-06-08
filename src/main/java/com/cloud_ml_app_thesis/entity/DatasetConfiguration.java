package com.cloud_ml_app_thesis.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="dataset_configurations")
public class DatasetConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String basicAttributesColumns;

    @Column
    private String targetColumn;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;
}
