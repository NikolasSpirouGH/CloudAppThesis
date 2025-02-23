package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.enumeration.status.DatasetConfigurationStatus;
import com.cloud_ml_app_thesis.util.ValidationUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="dataset_configurations")
public class DatasetConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String basicAttributesColumns;

    @Column
    private String targetColumn;

    @Column(name = "upload_date", nullable = false)
    private ZonedDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DatasetConfigurationStatus status;

    @JsonBackReference
    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    public DatasetConfiguration(Dataset dataset) {
        this.dataset = dataset;
    }
    public DatasetConfiguration(String basicAttributesColumns, String targetColumn,
                                ZonedDateTime uploadDate, DatasetConfigurationStatus status, Dataset dataset) {
        this.id = null;
        this.basicAttributesColumns = basicAttributesColumns;
        this.targetColumn = targetColumn;
        this.uploadDate = uploadDate;
        this.status = status;
        this.dataset = dataset;
    }
    public DatasetConfiguration(String basicAttributesColumns, String targetColumn,
                                            ZonedDateTime uploadDate, Dataset dataset) {
        this.id = null;
        this.basicAttributesColumns = basicAttributesColumns;
        this.targetColumn = targetColumn;
        this.uploadDate = uploadDate;
        if(ValidationUtil.stringExists(basicAttributesColumns) || ValidationUtil.stringExists(targetColumn)){
            this.status = DatasetConfigurationStatus.CUSTOM;
        } else{
            this.status = DatasetConfigurationStatus.DEFAULT;
        }
        this.dataset = dataset;
    }

    public void setBasicAttributesColumns(String columns){
        this.basicAttributesColumns = columns;
        if(ValidationUtil.stringExists(columns) || ValidationUtil.stringExists(this.targetColumn)){
                this.status = DatasetConfigurationStatus.DEFAULT;
                return;
        } else {
            this.status = DatasetConfigurationStatus.CUSTOM;
        }
    }
    public void setTargetColumn(String column){
        this.targetColumn = column;
        if(ValidationUtil.stringExists(column) || ValidationUtil.stringExists(this.basicAttributesColumns)){
                this.status = DatasetConfigurationStatus.DEFAULT;
        } else{
            this.status = DatasetConfigurationStatus.CUSTOM;
        }

    }
}
