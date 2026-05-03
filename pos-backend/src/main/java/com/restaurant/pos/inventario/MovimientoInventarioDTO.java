package com.restaurant.pos.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MovimientoInventarioDTO {

    public UUID id;
    public String tipo;
    public BigDecimal cantidad;
    public String motivo;
    public String proveedor;
    public String usuario;
    public LocalDateTime fechaHora;

    public static MovimientoInventarioDTO from(MovimientoInventario m) {
        MovimientoInventarioDTO dto = new MovimientoInventarioDTO();
        dto.id = m.id;
        dto.tipo = m.tipo != null ? m.tipo.name() : null;
        dto.cantidad = m.cantidad;
        dto.motivo = m.motivo;
        dto.proveedor = m.proveedor;
        dto.usuario = m.usuario != null ? m.usuario.username : null;
        dto.fechaHora = m.fechaHora;
        return dto;
    }
}
