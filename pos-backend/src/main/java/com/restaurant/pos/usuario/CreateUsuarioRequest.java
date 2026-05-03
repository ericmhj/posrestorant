package com.restaurant.pos.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateUsuarioRequest {

    @NotBlank(message = "El nombre es requerido")
    public String nombre;

    @NotBlank(message = "El apellido es requerido")
    public String apellido;

    @NotBlank(message = "El username es requerido")
    public String username;

    @NotBlank(message = "La contraseña es requerida")
    public String password;

    @NotNull(message = "El rol es requerido")
    public Rol rol;
}
