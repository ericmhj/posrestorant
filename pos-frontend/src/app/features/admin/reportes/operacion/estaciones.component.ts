import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ReporteFiltro } from '../shared/reporte.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-estaciones',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="items.length > 0" class="reporte-card">
      <div class="resumen-kpis">
        <div class="kpi">
          <span class="kpi-label">Productos analizados</span>
          <span class="kpi-value">{{ items.length }}</span>
        </div>
        <div class="kpi">
          <span class="kpi-label">Cuello de botella</span>
          <span class="kpi-value cuello">{{ cuelloBotella || '—' }}</span>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>Producto</th>
            <th>Estación</th>
            <th>Preparados</th>
            <th>Tiempo Prom.</th>
            <th>Mín</th>
            <th>Máx</th>
            <th>Varianza</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let item of items" [class.row-cuello]="item.esCuelloBotella">
            <td>{{ item.producto }}</td>
            <td>
              <span class="estacion-badge" [class.cocina]="item.estacion === 'COCINA'" [class.barra]="item.estacion === 'BARRA'">
                {{ item.estacion }}
              </span>
            </td>
            <td>{{ item.cantidadPreparada }}</td>
            <td>{{ item.tiempoPromedioMinutos | number:'1.1-1' }} min</td>
            <td>{{ formatSeg(item.tiempoMinSegundos) }}</td>
            <td>{{ formatSeg(item.tiempoMaxSegundos) }}</td>
            <td>{{ formatSeg(item.varianzaSegundos) }}</td>
            <td>
              <span *ngIf="item.esCuelloBotella" class="badge-cuello">⚠️ Cuello de botella</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">
      No hay datos de preparación. Los items necesitan haber pasado por estado LISTO.
    </div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .resumen-kpis { display: flex; gap: 2rem; margin-bottom: 1rem; }
    .kpi { display: flex; flex-direction: column; gap: .2rem; }
    .kpi-label { font-size: .8rem; color: #888; text-transform: uppercase; }
    .kpi-value { font-size: 1.3rem; font-weight: 700; color: #2c3e50; }
    .kpi-value.cuello { color: #e74c3c; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; font-size: .85rem; }
    .table th { background: #f0f0f0; }
    .row-cuello { background: #fdf2f2; }
    .estacion-badge { padding: .15rem .5rem; border-radius: 8px; font-size: .75rem; font-weight: 600; }
    .estacion-badge.cocina { background: #eaf4fb; color: #2980b9; }
    .estacion-badge.barra { background: #fef9e7; color: #d68910; }
    .badge-cuello { background: #e74c3c; color: #fff; padding: .2rem .5rem; border-radius: 8px; font-size: .75rem; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
  `]
})
export class EstacionesComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  items: any[] = [];
  sinDatos = false;
  cuelloBotella = '';

  constructor(private http: HttpClient) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    let params = new HttpParams()
      .set('desde', this.filtro.desde)
      .set('hasta', this.filtro.hasta);
    if (this.filtro.estacion) params = params.set('estacion', this.filtro.estacion);

    this.http.get<any[]>(`${environment.apiUrl}/api/v1/reportes/estaciones`, { params })
      .subscribe({
        next: (r) => {
          this.items = r || [];
          this.sinDatos = this.items.length === 0;
          const cuello = this.items.find(i => i.esCuelloBotella);
          this.cuelloBotella = cuello ? cuello.producto : '';
        },
        error: () => { this.sinDatos = true; }
      });
  }

  formatSeg(seg: number): string {
    if (!seg) return '0s';
    if (seg < 60) return `${Math.round(seg)}s`;
    return `${(seg / 60).toFixed(1)} min`;
  }
}
