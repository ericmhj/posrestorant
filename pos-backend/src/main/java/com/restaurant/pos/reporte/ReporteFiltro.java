package com.restaurant.pos.reporte;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filtro centralizado para todos los reportes.
 * Todos los campos son opcionales excepto desde/hasta.
 */
public class ReporteFiltro {

    public LocalDateTime desde;
    public LocalDateTime hasta;
    public String turno;       // ALMUERZO, CENA, NOCHE
    public UUID meseroId;
    public String estacion;    // COCINA, BARRA
    public UUID categoriaId;

    public ReporteFiltro() {}

    public ReporteFiltro(LocalDateTime desde, LocalDateTime hasta,
                         String turno, String meseroId,
                         String estacion, String categoriaId) {
        this.desde = desde;
        this.hasta = hasta;
        this.turno = turno != null && !turno.isBlank() ? turno : null;
        this.meseroId = meseroId != null && !meseroId.isBlank() ? UUID.fromString(meseroId) : null;
        this.estacion = estacion != null && !estacion.isBlank() ? estacion : null;
        this.categoriaId = categoriaId != null && !categoriaId.isBlank() ? UUID.fromString(categoriaId) : null;
    }

    /**
     * Returns the hour range for the turno filter.
     * ALMUERZO: 11:00-16:00, CENA: 16:00-22:00, NOCHE: 22:00-04:00
     */
    public int[] getHorasRango() {
        if (turno == null) return null;
        return switch (turno.toUpperCase()) {
            case "ALMUERZO" -> new int[]{11, 16};
            case "CENA" -> new int[]{16, 22};
            case "NOCHE" -> new int[]{22, 4};
            default -> null;
        };
    }
}
