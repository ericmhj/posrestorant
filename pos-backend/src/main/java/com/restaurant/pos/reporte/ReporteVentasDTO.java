package com.restaurant.pos.reporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ReporteVentasDTO {

    public BigDecimal totalVentas;
    public Long numeroCuentas;
    public BigDecimal ticketPromedio;
    public Map<String, BigDecimal> desglosePago;
    public LocalDateTime desde;
    public LocalDateTime hasta;

    public ReporteVentasDTO() {
        this.totalVentas = BigDecimal.ZERO;
        this.numeroCuentas = 0L;
        this.ticketPromedio = BigDecimal.ZERO;
        this.desglosePago = new java.util.HashMap<>();
    }
}
