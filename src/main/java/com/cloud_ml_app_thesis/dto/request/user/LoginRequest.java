package com.cloud_ml_app_thesis.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    String username;
    String password;
}
