import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { PeriodoFiltro, PeriodoFiltroHelper } from '../models/periodo-filtro.model';
import { Turno, UsuarioDTO } from '../models/turno.model';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  private apiUrl = `${environment.apiUrl}/api/v1/reportes`;

  constructor(private http: HttpClient) {}

  // -------------------------------------------------------
  // Filtros
  // -------------------------------------------------------

  getTurnos(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/filtros/turnos`);
  }

  getMeseros(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(`${this.apiUrl}/filtros/meseros`);
  }

  // -------------------------------------------------------
  // Helper Methods
  // -------------------------------------------------------

  validarFiltro(filtro: PeriodoFiltro): boolean {
    return PeriodoFiltroHelper.esValido(filtro);
  }

  formatearFecha(fecha: Date): string {
    return PeriodoFiltroHelper.formatearFecha(fecha);
  }

  crearFiltroDefault(): PeriodoFiltro {
    return PeriodoFiltroHelper.crearUltimos30Dias();
  }

  // -------------------------------------------------------
  // Parámetros HTTP
  // -------------------------------------------------------

  private construirParams(filtro: PeriodoFiltro): HttpParams {
    let params = new HttpParams();
    params = params.set('desde', this.formatearFecha(filtro.desde));
    params = params.set('hasta', this.formatearFecha(filtro.hasta));
    if (filtro.turno) {
      params = params.set('turno', filtro.turno);
    }
    if (filtro.usuarioId) {
      params = params.set('usuarioId', filtro.usuarioId);
    }
    return params;
  }

  protected getParams(filtro: PeriodoFiltro): HttpParams {
    return this.construirParams(filtro);
  }
}
