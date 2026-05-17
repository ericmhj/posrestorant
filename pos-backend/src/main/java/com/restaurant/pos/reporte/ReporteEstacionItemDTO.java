package com.restaurant.pos.reporte;

public class ReporteEstacionItemDTO {
    public String producto;
    public String estacion;
    public Long cantidadPreparada;
    public Double tiempoPromedioSegundos;
    public Double tiempoPromedioMinutos;
    public Double tiempoMinSegundos;
    public Double tiempoMaxSegundos;
    public Double varianzaSegundos;
    public Boolean esCuelloBotella;

    public ReporteEstacionItemDTO() {}
}
