package com.restaurant.pos.producto;

import jakarta.validation.constraints.NotBlank;

public class CreateCategoriaRequest {
    @NotBlank(message = "El nombre de la categoría es requerido")
    public String nombre;
    public String descripcion;
}
