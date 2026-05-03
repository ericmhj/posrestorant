package com.restaurant.pos.cuenta;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.restaurant.pos.pedido.Pedido;

public class CuentaDetalleDTO {

    public UUID id;
    public UUID mesaId;
    public String mesaNombre;
    public UUID meseroId;
    public String estado;
    public LocalDateTime abiertaEn;
    public LocalDateTime cerradaEn;
    public BigDecimal total;
    public Long tiempoAbierta; // seconds
    public List<PedidoDTO> pedidos;

    public static CuentaDetalleDTO from(Cuenta c, List<Pedido> pedidos) {
        CuentaDetalleDTO dto = new CuentaDetalleDTO();
        dto.id = c.id;
        if (c.mesa != null) {
            dto.mesaId = c.mesa.id;
            dto.mesaNombre = c.mesa.nombre;
        }
        if (c.mesero != null) dto.meseroId = c.mesero.id;
        dto.estado = c.estado != null ? c.estado.name() : null;
        dto.abiertaEn = c.abiertaEn;
        dto.cerradaEn = c.cerradaEn;
        dto.total = c.total;
        if (c.abiertaEn != null && c.cerradaEn == null) {
            dto.tiempoAbierta = Duration.between(c.abiertaEn, LocalDateTime.now()).getSeconds();
        }
        dto.pedidos = pedidos != null
                ? pedidos.stream().map(PedidoDTO::from).collect(Collectors.toList())
                : List.of();
        return dto;
    }
}
