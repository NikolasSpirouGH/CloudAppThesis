package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.enumeration.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private UserRole name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}
