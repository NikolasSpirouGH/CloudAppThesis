package com.cloud_ml_app_thesis.entity.accessibility;

import com.cloud_ml_app_thesis.entity.Model;
import com.cloud_ml_app_thesis.enumeration.accessibility.ModelAccessibilityEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name= "CONST_MODEL_ACCESSIBILITES")
public class ModelAccessibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModelAccessibilityEnum name;

    @Column(nullable = false)
    private String description;
}
