package com.restaurant.pos.producto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateProductoRequest {
    public String nombre;
    public String descripcion;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    public BigDecimal precio;

    public UUID categoriaId;
    public Estacion estacion;
}
