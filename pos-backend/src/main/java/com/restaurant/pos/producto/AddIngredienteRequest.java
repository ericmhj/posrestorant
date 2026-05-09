package com.restaurant.pos.producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class AddIngredienteRequest {

    @NotNull(message = "El item de inventario es requerido")
    public UUID itemInventarioId;

    @NotNull(message = "La cantidad es requerida")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    public BigDecimal cantidad;
}
