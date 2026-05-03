package com.restaurant.pos.inventario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateItemInventarioRequest {

    @NotBlank(message = "El nombre es requerido")
    public String nombre;

    public String unidadMedida;

    @NotNull(message = "El stock mínimo es requerido")
    @DecimalMin(value = "0", message = "El stock mínimo no puede ser negativo")
    public BigDecimal stockMinimo = BigDecimal.ZERO;

    public BigDecimal stockMaximo;
}
