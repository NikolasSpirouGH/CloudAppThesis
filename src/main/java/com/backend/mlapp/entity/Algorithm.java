package com.backend.mlapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "algorithms")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Algorithm {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "algorithm_id")
    private Integer id;

    @Column(name = "algorithm_name")
    private String name;

    /*
    @OneToMany(mappedBy = "algorithm")
    private List<Training> training*/;
/*
    @Column(name = "training_user")
    private String user;*/

/*
    @Column(name = "algorithm_config_param")
    private String configParameters;*/
}
