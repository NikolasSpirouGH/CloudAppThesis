package com.cloud_ml_app_thesis.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private Integer age;

    @Column
    private String profession;

    @Column
    private String country;

    @Column
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis
.model.UserRole role;

    @Column
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis
.model.UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.cloud_ml_app_thesis
.model.Training> trainings;
}