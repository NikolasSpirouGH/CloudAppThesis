package com.cloud_ml_app_thesis.entity.status;

import com.cloud_ml_app_thesis.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis.enumeration.status.UserStatus name;

    @Column(name = "description")
    private String description;

    @OneToOne(mappedBy = "status")
    private User user;
}
