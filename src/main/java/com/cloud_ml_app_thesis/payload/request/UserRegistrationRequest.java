package com.cloud_ml_app_thesis.payload.request;

import jakarta.validation.constraints.*;


public record UserRegistrationRequest(
    @NotBlank
    @NotNull
    @NotEmpty
    @Email
    String email,

    @NotBlank
    @NotNull
    @NotEmpty
    String firstName,

    @NotBlank
    @NotNull
    @NotEmpty
    String lastName,
    @NotBlank
    @NotNull
    @NotEmpty
    String username,

    @NotBlank
    @NotNull
    @NotEmpty
    String password,

    @NotBlank
    @NotNull
    @NotEmpty
    String confirmPassword,

    @NotBlank
    @NotNull
    @NotEmpty
    String country,

    @NotBlank
    @NotNull
    @NotEmpty
    String profession,

    @Min(18)
    @Max(99)
    Integer age
){}