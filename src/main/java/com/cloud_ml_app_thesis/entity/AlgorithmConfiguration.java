package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.util.ValidationUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="algorithm_configurations")
public class AlgorithmConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "algorithm_id")
    private Algorithm algorithm;

    @Column
    private String options;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "algorithm_type_id")
    private AlgorithmType algorithmType;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public AlgorithmConfiguration(Algorithm algorithm){
        this.algorithm = algorithm;
    }

    public void setOptions(String options){
        if (ValidationUtil.stringExists(options)) {
            this.options = options.trim();
        } else if (algorithm != null) {
            this.options = algorithm.getOptions().trim();
        } else {
            this.options = null;
        }
    }

}

