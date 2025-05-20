package com.cloud_ml_app_thesis.entity;

import com.cloud_ml_app_thesis.entity.dataset.Dataset;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Self-referencing Many-to-Many for hierarchical categories
    @ManyToMany
    @JoinTable(
            name = "category_hierarchy",
            joinColumns = @JoinColumn(name = "child_category_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_category_id")
    )
    private Set<Category> parentCategories;

    @ManyToMany(mappedBy = "parentCategories")
    private Set<Category> childCategories;

    // Link categories to ML Models
    @ManyToMany(mappedBy = "categories")
    private Set<Model> models;

    // Link categories to Datasets
    @ManyToMany(mappedBy = "categories")
    private Set<Dataset> datasets;

    @Column(nullable = false)
    private boolean deleted = false;

}
