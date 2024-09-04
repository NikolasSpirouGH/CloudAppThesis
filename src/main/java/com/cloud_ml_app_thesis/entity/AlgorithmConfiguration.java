package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.entity.Algorithm;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}
