export interface PeriodoFiltro {
  desde: Date;
  hasta: Date;
  turno?: string; // ALMUERZO | CENA | NOCHE
  usuarioId?: string;
}

export class PeriodoFiltroHelper {
  static crearHoy(): PeriodoFiltro {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const hasta = new Date(hoy);
    hasta.setHours(23, 59, 59, 999);
    return { desde: hoy, hasta };
  }

  static crearEstaSemana(): PeriodoFiltro {
    const hoy = new Date();
    const primero = new Date(hoy.setDate(hoy.getDate() - hoy.getDay()));
    primero.setHours(0, 0, 0, 0);
    const ultima = new Date(primero);
    ultima.setDate(ultima.getDate() + 6);
    ultima.setHours(23, 59, 59, 999);
    return { desde: primero, hasta: ultima };
  }

  static crearEsteMes(): PeriodoFiltro {
    const ahora = new Date();
    const desde = new Date(ahora.getFullYear(), ahora.getMonth(), 1);
    desde.setHours(0, 0, 0, 0);
    const hasta = new Date(ahora.getFullYear(), ahora.getMonth() + 1, 0);
    hasta.setHours(23, 59, 59, 999);
    return { desde, hasta };
  }

  static crearUltimos30Dias(): PeriodoFiltro {
    const hasta = new Date();
    hasta.setHours(23, 59, 59, 999);
    const desde = new Date(hasta);
    desde.setDate(desde.getDate() - 30);
    desde.setHours(0, 0, 0, 0);
    return { desde, hasta };
  }

  static esValido(filtro: PeriodoFiltro): boolean {
    return filtro && filtro.desde && filtro.hasta && filtro.desde < filtro.hasta;
  }

  static formatearFecha(fecha: Date): string {
    return fecha.toISOString().slice(0, 16);
  }
}
