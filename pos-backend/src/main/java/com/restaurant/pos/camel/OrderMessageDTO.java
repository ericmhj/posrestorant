package com.restaurant.pos.camel;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderMessageDTO {

    public UUID itemPedidoId;
    public UUID pedidoId;
    public UUID cuentaId;
    public UUID mesaId;
    public String mesaNombre;
    public String productoNombre;
    public Integer cantidad;
    public String modificadores;
    public String estacion; // COCINA or BARRA
    public LocalDateTime createdAt;

    public OrderMessageDTO() {}

    public OrderMessageDTO(UUID itemPedidoId, UUID pedidoId, UUID cuentaId,
                            UUID mesaId, String mesaNombre, String productoNombre,
                            Integer cantidad, String modificadores,
                            String estacion, LocalDateTime createdAt) {
        this.itemPedidoId = itemPedidoId;
        this.pedidoId = pedidoId;
        this.cuentaId = cuentaId;
        this.mesaId = mesaId;
        this.mesaNombre = mesaNombre;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.modificadores = modificadores;
        this.estacion = estacion;
        this.createdAt = createdAt;
    }
}
