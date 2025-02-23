package com.cloud_ml_app_thesis.repository.accessibility;

import com.cloud_ml_app_thesis.entity.status.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserStatusRepository extends JpaRepository<UserStatus, Integer> {
}
