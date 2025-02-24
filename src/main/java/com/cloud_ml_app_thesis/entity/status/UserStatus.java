package com.cloud_ml_app_thesis.entity.status;

import com.cloud_ml_app_thesis.entity.User;
import com.cloud_ml_app_thesis.enumeration.status.UserStatusEnum;
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
    private UserStatusEnum name = UserStatusEnum.ACTIVE;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
