package com.cloud_ml_app_thesis.entity.status;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "model_status")
public class ModelStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis.enumeration.status.ModelStatus name;
    @Column(name = "description", length = 1000)
    private String description;
}
