import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ReporteFiltro } from '../shared/reporte.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-horas-pico',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="horas.length > 0" class="reporte-card">
      <!-- Barra visual -->
      <div class="chart-bars">
        <div *ngFor="let h of horas" class="bar-item" [class.pico]="h.esPico">
          <div class="bar" [style.height.%]="getBarHeight(h.numeroCuentas)"></div>
          <span class="bar-label">{{ h.hora }}h</span>
        </div>
      </div>

      <!-- Tabla detalle -->
      <table class="table">
        <thead>
          <tr>
            <th>Hora</th>
            <th>Cuentas</th>
            <th>Ingresos</th>
            <th>Tiempo Prom. (min)</th>
            <th>Pico</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let h of horas" [class.row-pico]="h.esPico">
            <td>{{ h.rangoHora }}</td>
            <td>{{ h.numeroCuentas }}</td>
            <td>$ {{ h.ingresos | number:'1.2-2' }}</td>
            <td>{{ h.tiempoPromedioServicioMinutos | number:'1.0-0' }}</td>
            <td>{{ h.esPico ? '🔥 PICO' : '' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">No hay datos de horas en el período seleccionado.</div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .chart-bars { display: flex; gap: 4px; align-items: flex-end; height: 120px; margin-bottom: 1.5rem; padding: 0 .5rem; border-bottom: 1px solid #ddd; }
    .bar-item { display: flex; flex-direction: column; align-items: center; flex: 1; }
    .bar { width: 100%; min-width: 16px; background: #3498db; border-radius: 3px 3px 0 0; transition: height .3s; }
    .bar-item.pico .bar { background: #e74c3c; }
    .bar-label { font-size: .7rem; color: #666; margin-top: .2rem; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; font-size: .9rem; }
    .table th { background: #f0f0f0; }
    .row-pico { background: #fdf2f2; font-weight: 600; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
  `]
})
export class HorasPicoComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  horas: any[] = [];
  sinDatos = false;
  private maxCuentas = 1;

  constructor(private http: HttpClient) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    let params = new HttpParams()
      .set('desde', this.filtro.desde)
      .set('hasta', this.filtro.hasta);
    if (this.filtro.turno) params = params.set('turno', this.filtro.turno);
    if (this.filtro.estacion) params = params.set('estacion', this.filtro.estacion);

    this.http.get<any[]>(`${environment.apiUrl}/api/v1/reportes/horas-pico`, { params })
      .subscribe({
        next: (r) => {
          this.horas = r || [];
          this.sinDatos = this.horas.length === 0;
          this.maxCuentas = Math.max(...this.horas.map(h => h.numeroCuentas), 1);
        },
        error: () => { this.sinDatos = true; }
      });
  }

  getBarHeight(cuentas: number): number {
    return (cuentas / this.maxCuentas) * 100;
  }
}
