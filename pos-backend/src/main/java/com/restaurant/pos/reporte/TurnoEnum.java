package com.restaurant.pos.reporte;

public enum TurnoEnum {
    ALMUERZO("11:00", "15:00"),
    CENA("17:00", "23:00"),
    NOCHE("23:00", "11:00");

    private final String horaInicio;
    private final String horaFin;

    TurnoEnum(String horaInicio, String horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public static TurnoEnum fromString(String turno) {
        if (turno == null) {
            return null;
        }
        try {
            return TurnoEnum.valueOf(turno.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
