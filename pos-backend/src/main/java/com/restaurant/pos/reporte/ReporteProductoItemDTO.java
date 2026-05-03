package com.restaurant.pos.reporte;

import java.math.BigDecimal;

public class ReporteProductoItemDTO {

    public String nombre;
    public Long cantidadVendida;
    public BigDecimal ingresos;
    public String categoria;

    public ReporteProductoItemDTO(String nombre, Long cantidadVendida,
                                   BigDecimal ingresos, String categoria) {
        this.nombre = nombre;
        this.cantidadVendida = cantidadVendida;
        this.ingresos = ingresos;
        this.categoria = categoria;
    }
}
