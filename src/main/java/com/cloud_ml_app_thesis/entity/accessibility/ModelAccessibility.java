package com.cloud_ml_app_thesis.entity.accessibility;

import com.cloud_ml_app_thesis.entity.Model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name= "model_accessibilities")
public class ModelAccessibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis.enumeration.accessibility.ModelAccessibility name;
    @Column
    private String description;

    @OneToOne(mappedBy = "modelAccessibility")
    private Model model;
}
