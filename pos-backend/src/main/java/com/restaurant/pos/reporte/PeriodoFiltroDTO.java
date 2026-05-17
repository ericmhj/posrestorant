package com.restaurant.pos.reporte;

import java.time.LocalDateTime;
import java.util.UUID;

public class PeriodoFiltroDTO {

    public LocalDateTime desde;
    public LocalDateTime hasta;
    public String turno; // ALMUERZO, CENA, NOCHE (opcional)
    public UUID usuarioId; // Para filtrar por usuario/mesero (opcional)

    public PeriodoFiltroDTO() {}

    public PeriodoFiltroDTO(LocalDateTime desde, LocalDateTime hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public PeriodoFiltroDTO(LocalDateTime desde, LocalDateTime hasta, String turno, UUID usuarioId) {
        this.desde = desde;
        this.hasta = hasta;
        this.turno = turno;
        this.usuarioId = usuarioId;
    }

    public boolean esValido() {
        if (desde == null || hasta == null) {
            return false;
        }
        return desde.isBefore(hasta);
    }

    @Override
    public String toString() {
        return "PeriodoFiltroDTO{" +
                "desde=" + desde +
                ", hasta=" + hasta +
                ", turno='" + turno + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}
