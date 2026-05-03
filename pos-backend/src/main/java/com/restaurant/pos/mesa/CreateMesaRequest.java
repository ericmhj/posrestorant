package com.restaurant.pos.mesa;

import jakarta.validation.constraints.NotBlank;

public class CreateMesaRequest {
    @NotBlank(message = "El nombre de la mesa es requerido")
    public String nombre;
}
