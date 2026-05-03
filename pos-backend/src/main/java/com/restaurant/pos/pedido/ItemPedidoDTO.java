package com.restaurant.pos.pedido;

import com.restaurant.pos.producto.Estacion;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class ItemPedidoDTO {

    public UUID id;
    public UUID pedidoId;
    public UUID productoId;
    public String productoNombre;
    public Integer cantidad;
    public BigDecimal precioUnitario;
    public String modificadores;
    public String estado;
    public String estacion;
    public String mesaNombre;
    public Long tiempoEspera; // seconds since created_at
    public Boolean demorado;

    public static ItemPedidoDTO from(ItemPedido item) {
        ItemPedidoDTO dto = new ItemPedidoDTO();
        dto.id = item.id;
        if (item.pedido != null) dto.pedidoId = item.pedido.id;
        if (item.producto != null) {
            dto.productoId = item.producto.id;
            dto.productoNombre = item.producto.nombre;
            dto.estacion = item.producto.estacion != null ? item.producto.estacion.name() : null;
        }
        dto.cantidad = item.cantidad;
        dto.precioUnitario = item.precioUnitario;
        dto.modificadores = item.modificadores;
        dto.estado = item.estado != null ? item.estado.name() : null;

        if (item.createdAt != null) {
            long seconds = Duration.between(item.createdAt, LocalDateTime.now()).getSeconds();
            dto.tiempoEspera = seconds;
            // KDS: demorado after 15 min; BDS: after 10 min
            Estacion estacion = item.producto != null ? item.producto.estacion : null;
            if (estacion == Estacion.COCINA) {
                dto.demorado = seconds > 900;
            } else if (estacion == Estacion.BARRA) {
                dto.demorado = seconds > 600;
            } else {
                dto.demorado = false;
            }
        }
        return dto;
    }
}
