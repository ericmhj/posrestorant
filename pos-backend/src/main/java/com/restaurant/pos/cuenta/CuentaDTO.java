package com.restaurant.pos.cuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CuentaDTO {

    public UUID id;
    public UUID mesaId;
    public String mesaNombre;
    public UUID meseroId;
    public String estado;
    public LocalDateTime abiertaEn;
    public LocalDateTime cerradaEn;
    public BigDecimal total;

    public static CuentaDTO from(Cuenta c) {
        CuentaDTO dto = new CuentaDTO();
        dto.id = c.id;
        if (c.mesa != null) {
            dto.mesaId = c.mesa.id;
            dto.mesaNombre = c.mesa.nombre;
        }
        if (c.mesero != null) {
            dto.meseroId = c.mesero.id;
        }
        dto.estado = c.estado != null ? c.estado.name() : null;
        dto.abiertaEn = c.abiertaEn;
        dto.cerradaEn = c.cerradaEn;
        dto.total = c.total;
        return dto;
    }
}
