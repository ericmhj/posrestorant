package com.restaurant.pos.cuenta;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CobroRequest {

    @NotNull(message = "El método de pago es requerido")
    public MetodoPago metodoPago;

    public BigDecimal montoRecibido; // required when metodoPago = EFECTIVO

    public enum MetodoPago {
        EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO
    }
}
