package com.restaurant.pos.reporte;

import java.math.BigDecimal;
import java.util.UUID;

public class ReporteMeseroItemDTO {
    public UUID meseroId;
    public String nombre;
    public String apellido;
    public Long numeroCuentas;
    public BigDecimal ingresosTotales;
    public BigDecimal ticketPromedio;
    public Double tiempoPromedioCuentaMinutos;
    public Long itemsCancelados;
    public Double tasaCancelacion;

    public ReporteMeseroItemDTO() {}

    public ReporteMeseroItemDTO(UUID meseroId, String nombre, String apellido,
                                 Long numeroCuentas, BigDecimal ingresosTotales,
                                 BigDecimal ticketPromedio, Double tiempoPromedio,
                                 Long itemsCancelados, Double tasaCancelacion) {
        this.meseroId = meseroId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroCuentas = numeroCuentas;
        this.ingresosTotales = ingresosTotales;
        this.ticketPromedio = ticketPromedio;
        this.tiempoPromedioCuentaMinutos = tiempoPromedio;
        this.itemsCancelados = itemsCancelados;
        this.tasaCancelacion = tasaCancelacion;
    }
}
