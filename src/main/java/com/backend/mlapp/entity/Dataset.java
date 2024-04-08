package com.backend.mlapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="datasets")
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String fileName;

    @Column
    private String urlPath;

    @Column
    private String trainingColumns;

    @Column
    private String targetColumn;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

}
