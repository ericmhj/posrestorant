package com.restaurant.pos.auth;

import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {

    @NotBlank(message = "El username es requerido")
    public String username;

    @NotBlank(message = "La contraseña es requerida")
    public String password;
}
