package com.restaurant.pos.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @JsonProperty("username") @NotBlank(message = "El username es requerido") String username,
        @JsonProperty("password") @NotBlank(message = "La contraseña es requerida") String password
) {}
