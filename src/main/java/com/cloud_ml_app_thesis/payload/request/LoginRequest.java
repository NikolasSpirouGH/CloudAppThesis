package com.cloud_ml_app_thesis.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull @NotBlank @NotEmpty String username, @NotNull @NotBlank @NotEmpty String password){}

