package com.restaurant.pos.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ItemInventarioDTO {

    public UUID id;
    public String nombre;
    public String unidadMedida;
    public BigDecimal stockActual;
    public BigDecimal stockMinimo;
    public BigDecimal stockMaximo;
    public BigDecimal stockDisponible;
    public Boolean alertaMinimo;
    public LocalDateTime updatedAt;
    public UUID categoriaId;
    public String categoriaNombre;

    public static ItemInventarioDTO from(ItemInventario item, BigDecimal reservado) {
        ItemInventarioDTO dto = new ItemInventarioDTO();
        dto.id = item.id;
        dto.nombre = item.nombre;
        dto.unidadMedida = item.unidadMedida;
        dto.stockActual = item.stockActual;
        dto.stockMinimo = item.stockMinimo;
        dto.stockMaximo = item.stockMaximo;
        dto.stockDisponible = item.stockActual.subtract(reservado != null ? reservado : BigDecimal.ZERO);
        dto.alertaMinimo = item.stockActual.compareTo(item.stockMinimo) < 0;
        dto.updatedAt = item.updatedAt;
        if (item.categoria != null) {
            dto.categoriaId = item.categoria.id;
            dto.categoriaNombre = item.categoria.nombre;
        }
        return dto;
    }
}
