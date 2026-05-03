package com.restaurant.pos.producto;

import java.util.UUID;

public class CategoriaDTO {

    public UUID id;
    public String nombre;
    public String descripcion;
    public Long totalProductos;

    public static CategoriaDTO from(Categoria c) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.id = c.id;
        dto.nombre = c.nombre;
        dto.descripcion = c.descripcion;
        dto.totalProductos = Categoria.countProductosByCategoriaId(c.id);
        return dto;
    }
}
