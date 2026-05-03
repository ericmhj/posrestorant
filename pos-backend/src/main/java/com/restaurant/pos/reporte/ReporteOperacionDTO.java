package com.restaurant.pos.reporte;

public class ReporteOperacionDTO {

    public Double tiempoPromedioCocinaSegundos;
    public Double tiempoPromedioBarraSegundos;
    public Long totalPedidos;
    public Long totalCancelaciones;

    public ReporteOperacionDTO() {
        this.tiempoPromedioCocinaSegundos = 0.0;
        this.tiempoPromedioBarraSegundos = 0.0;
        this.totalPedidos = 0L;
        this.totalCancelaciones = 0L;
    }
}
