package com.cloud_ml_app_thesis.repository.status;

import com.cloud_ml_app_thesis.enumeration.status.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserStatusRepository extends JpaRepository<com.cloud_ml_app_thesis.entity.status.UserStatus, Integer> {
    Optional<com.cloud_ml_app_thesis.entity.status.UserStatus> findByName(UserStatusEnum name);
}
