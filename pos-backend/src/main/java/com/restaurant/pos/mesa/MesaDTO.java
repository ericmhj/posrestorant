package com.restaurant.pos.mesa;

import java.util.UUID;

public class MesaDTO {

    public UUID id;
    public String nombre;
    public String estado;
    public UUID cuentaId;
    public Double totalAcumulado;
    public Long tiempoAbierta; // seconds

    public static MesaDTO from(Mesa mesa) {
        MesaDTO dto = new MesaDTO();
        dto.id = mesa.id;
        dto.nombre = mesa.nombre;
        dto.estado = mesa.estado != null ? mesa.estado.name() : null;
        return dto;
    }
}
