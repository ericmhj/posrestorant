package com.restaurant.pos.inventario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RegistrarMovimientoRequest {

    @NotNull(message = "El tipo de movimiento es requerido")
    public TipoMovimiento tipo;

    @NotNull(message = "La cantidad es requerida")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    public BigDecimal cantidad;

    public String motivo;    // required for AJUSTE and MERMA
    public String proveedor; // optional for ENTRADA
}
