package com.cloud_ml_app_thesis.entity;


import com.cloud_ml_app_thesis.enumeration.accessibility.DatasetAccessibility;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="datasets")
public class Dataset {
    //CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "upload_date", nullable = false)
    private ZonedDateTime uploadDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DatasetAccessibility status = DatasetAccessibility.PRIVATE;

    @Column(name = "description")
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatasetConfiguration> datasetConfigurations;
}
