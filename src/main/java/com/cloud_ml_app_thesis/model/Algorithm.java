package com.cloud_ml_app_thesis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "algorithms")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Algorithm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String name;

    @Column
    private String defaultParameters;

    //orphanRemoval = false cause the algorithm can be removed from the app but if the training or the model doesn't we can remove their configuration
    @OneToMany(mappedBy = "algorithm_configurations", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<AlgorithmConfiguration> algorithmConfigurations;

}
