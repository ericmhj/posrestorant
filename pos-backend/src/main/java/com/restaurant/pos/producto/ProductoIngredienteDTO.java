package com.restaurant.pos.producto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductoIngredienteDTO {
    public UUID id;
    public UUID itemInventarioId;
    public String itemInventarioNombre;
    public String unidadMedida;
    public BigDecimal cantidad;

    public static ProductoIngredienteDTO from(ProductoIngrediente pi) {
        ProductoIngredienteDTO dto = new ProductoIngredienteDTO();
        dto.id = pi.id;
        dto.itemInventarioId = pi.itemInventario.id;
        dto.itemInventarioNombre = pi.itemInventario.nombre;
        dto.unidadMedida = pi.itemInventario.unidadMedida;
        dto.cantidad = pi.cantidad;
        return dto;
    }
}
