package com.restaurant.pos.reporte;

import java.math.BigDecimal;

public class ReporteHoraPicoItemDTO {
    public Integer hora;
    public String rangoHora;
    public Long numeroCuentas;
    public BigDecimal ingresos;
    public Double tiempoPromedioServicioMinutos;
    public String estacionMasCongestionada;
    public Boolean esPico;

    public ReporteHoraPicoItemDTO() {}

    public ReporteHoraPicoItemDTO(Integer hora, Long numeroCuentas, BigDecimal ingresos,
                                   Double tiempoPromedio, String estacionCongestionada) {
        this.hora = hora;
        this.rangoHora = String.format("%02d:00 - %02d:59", hora, hora);
        this.numeroCuentas = numeroCuentas;
        this.ingresos = ingresos;
        this.tiempoPromedioServicioMinutos = tiempoPromedio;
        this.estacionMasCongestionada = estacionCongestionada;
        this.esPico = false;
    }
}
