package com.cloud_ml_app_thesis.entity.status;

import com.cloud_ml_app_thesis.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private com.cloud_ml_app_thesis.enumeration.status.UserStatus name = com.cloud_ml_app_thesis.enumeration.status.UserStatus.ACTIVE;

    @Column(name = "description")
    private String description;

    @OneToOne(mappedBy = "status")
    private User user;
}
