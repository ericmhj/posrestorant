package com.restaurant.pos.reporte;

import java.math.BigDecimal;

public class ReporteRentabilidadItemDTO {
    public String nombre;
    public String categoria;
    public Long cantidadVendida;
    public BigDecimal ingresosBrutos;
    public BigDecimal costoUnitario;
    public BigDecimal costoTotal;
    public BigDecimal margenBruto;
    public Double margenPorcentaje;
    public Integer ranking;

    public ReporteRentabilidadItemDTO() {}
}
