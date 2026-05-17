import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

export interface ReporteFiltro {
  desde: string;
  hasta: string;
  turno?: string;
  meseroId?: string;
  estacion?: string;
  categoriaId?: string;
}

@Injectable({ providedIn: 'root' })
export class ReporteService {

  private baseUrl = `${environment.apiUrl}/api/v1/reportes`;

  constructor(private http: HttpClient) {}

  getVentas(filtro: ReporteFiltro): Observable<any> {
    return this.http.get(`${this.baseUrl}/ventas`, { params: this.buildParams(filtro) });
  }

  getProductos(filtro: ReporteFiltro): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/productos`, { params: this.buildParams(filtro) });
  }

  getOperacion(filtro: ReporteFiltro): Observable<any> {
    return this.http.get(`${this.baseUrl}/operacion`, { params: this.buildParams(filtro) });
  }

  getInventario(filtro: ReporteFiltro): Observable<any> {
    return this.http.get(`${this.baseUrl}/inventario`, { params: this.buildParams(filtro) });
  }

  getMeseros(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/filtros/meseros`);
  }

  getCategorias(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/v1/categorias`);
  }

  exportUrl(tipo: string, filtro: ReporteFiltro): string {
    const params = this.buildParams(filtro);
    const token = sessionStorage.getItem('pos_token');
    return `${this.baseUrl}/${tipo}/export?${params.toString()}&token=${token}`;
  }

  private buildParams(filtro: ReporteFiltro): HttpParams {
    let params = new HttpParams()
      .set('desde', filtro.desde)
      .set('hasta', filtro.hasta);

    if (filtro.turno) params = params.set('turno', filtro.turno);
    if (filtro.meseroId) params = params.set('meseroId', filtro.meseroId);
    if (filtro.estacion) params = params.set('estacion', filtro.estacion);
    if (filtro.categoriaId) params = params.set('categoriaId', filtro.categoriaId);

    return params;
  }
}
