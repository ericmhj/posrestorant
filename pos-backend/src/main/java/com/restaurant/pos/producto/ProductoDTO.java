package com.restaurant.pos.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductoDTO {

    public UUID id;
    public String nombre;
    public String descripcion;
    public BigDecimal precio;
    public CategoriaDTO categoria;
    public String estacion;
    public Boolean activo;
    public String imagenUrl;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static ProductoDTO from(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.id = p.id;
        dto.nombre = p.nombre;
        dto.descripcion = p.descripcion;
        dto.precio = p.precio;
        dto.categoria = p.categoria != null ? CategoriaDTO.from(p.categoria) : null;
        dto.estacion = p.estacion != null ? p.estacion.name() : null;
        dto.activo = p.activo;
        dto.imagenUrl = p.imagenUrl;
        dto.createdAt = p.createdAt;
        dto.updatedAt = p.updatedAt;
        return dto;
    }
}
