package com.restaurant.pos.reporte;

import java.math.BigDecimal;
import java.util.UUID;

public class ReporteInventarioItemDTO {
    public UUID id;
    public String nombre;
    public String unidadMedida;
    public String categoria;
    public BigDecimal stockActual;
    public BigDecimal stockMinimo;
    public BigDecimal stockDisponible;
    public BigDecimal costoUnitario;
    public BigDecimal costoInventario;       // stockActual * costoUnitario
    public Double rotacionDias;              // días desde última salida
    public BigDecimal consumoPromedioDiario;
    public BigDecimal stockProyectadoSemana; // stockActual - (consumoPromedio * 7)
    public Boolean alertaMinimo;
    public Boolean alertaProyeccion;         // stockProyectado < stockMinimo

    public ReporteInventarioItemDTO() {}
}
