package com.cloud_ml_app_thesis.payload.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AuthResponse(@NotNull @NotBlank @NotEmpty String token) {
}
