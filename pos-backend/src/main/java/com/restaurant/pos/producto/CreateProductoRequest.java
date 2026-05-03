package com.restaurant.pos.producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateProductoRequest {

    @NotBlank(message = "El nombre es requerido")
    public String nombre;

    public String descripcion;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    public BigDecimal precio;

    @NotNull(message = "La categoría es requerida")
    public UUID categoriaId;

    @NotNull(message = "La estación es requerida")
    public Estacion estacion;
}
