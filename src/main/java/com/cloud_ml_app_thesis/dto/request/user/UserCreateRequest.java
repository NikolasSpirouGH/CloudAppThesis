package com.cloud_ml_app_thesis.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCreateRequest {

    private String firstName;
    private String lastName;
    private String password;
    private String confirmPassword;
    private String email;
    private String country;
    private String profession;
    private int age;
}
