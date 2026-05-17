import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ReporteFiltro } from '../shared/reporte.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-inventario-detallado',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="items.length > 0" class="reporte-card">
      <!-- KPIs resumen -->
      <div class="kpis">
        <div class="kpi">
          <span class="kpi-label">Items totales</span>
          <span class="kpi-value">{{ items.length }}</span>
        </div>
        <div class="kpi">
          <span class="kpi-label">Costo inventario</span>
          <span class="kpi-value">$ {{ costoTotal | number:'1.2-2' }}</span>
        </div>
        <div class="kpi">
          <span class="kpi-label">Alertas stock</span>
          <span class="kpi-value alerta">{{ alertasCount }}</span>
        </div>
        <div class="kpi">
          <span class="kpi-label">Riesgo proyección</span>
          <span class="kpi-value alerta">{{ proyeccionCount }}</span>
        </div>
      </div>

      <!-- Tabla -->
      <table class="table">
        <thead>
          <tr>
            <th>Item</th>
            <th>Categoría</th>
            <th>Stock</th>
            <th>Disponible</th>
            <th>Consumo/día</th>
            <th>Proyección 7d</th>
            <th>Rotación</th>
            <th>Costo inv.</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let item of items"
              [class.row-alerta]="item.alertaMinimo"
              [class.row-proyeccion]="item.alertaProyeccion && !item.alertaMinimo">
            <td>{{ item.nombre }} <small>({{ item.unidadMedida }})</small></td>
            <td>{{ item.categoria || '—' }}</td>
            <td>{{ item.stockActual | number:'1.1-1' }}</td>
            <td>{{ item.stockDisponible | number:'1.1-1' }}</td>
            <td>{{ item.consumoPromedioDiario | number:'1.2-2' }}</td>
            <td [class.negativo]="item.stockProyectadoSemana < 0">
              {{ item.stockProyectadoSemana | number:'1.1-1' }}
            </td>
            <td>{{ item.rotacionDias != null ? (item.rotacionDias | number:'1.0-0') + 'd' : '—' }}</td>
            <td>$ {{ item.costoInventario | number:'1.2-2' }}</td>
            <td>
              <span *ngIf="item.alertaMinimo" class="badge-rojo">⚠️ Bajo mín.</span>
              <span *ngIf="item.alertaProyeccion && !item.alertaMinimo" class="badge-amarillo">📉 Riesgo 7d</span>
              <span *ngIf="!item.alertaMinimo && !item.alertaProyeccion" class="badge-verde">✅ OK</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">No hay items de inventario.</div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .kpis { display: flex; gap: 2rem; flex-wrap: wrap; margin-bottom: 1rem; }
    .kpi { display: flex; flex-direction: column; gap: .2rem; }
    .kpi-label { font-size: .8rem; color: #888; text-transform: uppercase; }
    .kpi-value { font-size: 1.3rem; font-weight: 700; color: #2c3e50; }
    .kpi-value.alerta { color: #e74c3c; }
    .table { width: 100%; border-collapse: collapse; font-size: .85rem; }
    .table th, .table td { padding: .4rem .5rem; border: 1px solid #ddd; text-align: left; }
    .table th { background: #f0f0f0; font-size: .8rem; }
    .row-alerta { background: #fdf2f2; }
    .row-proyeccion { background: #fef9e7; }
    .negativo { color: #e74c3c; font-weight: 600; }
    .badge-rojo { background: #e74c3c; color: #fff; padding: .15rem .4rem; border-radius: 8px; font-size: .75rem; }
    .badge-amarillo { background: #f39c12; color: #fff; padding: .15rem .4rem; border-radius: 8px; font-size: .75rem; }
    .badge-verde { background: #27ae60; color: #fff; padding: .15rem .4rem; border-radius: 8px; font-size: .75rem; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
    small { color: #888; }
  `]
})
export class InventarioDetalladoComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  items: any[] = [];
  sinDatos = false;
  costoTotal = 0;
  alertasCount = 0;
  proyeccionCount = 0;

  constructor(private http: HttpClient) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    let params = new HttpParams()
      .set('desde', this.filtro.desde)
      .set('hasta', this.filtro.hasta);
    if (this.filtro.categoriaId) params = params.set('categoriaId', this.filtro.categoriaId);

    this.http.get<any[]>(`${environment.apiUrl}/api/v1/reportes/inventario-detallado`, { params })
      .subscribe({
        next: (r) => {
          this.items = r || [];
          this.sinDatos = this.items.length === 0;
          this.costoTotal = this.items.reduce((sum, i) => sum + (i.costoInventario || 0), 0);
          this.alertasCount = this.items.filter(i => i.alertaMinimo).length;
          this.proyeccionCount = this.items.filter(i => i.alertaProyeccion && !i.alertaMinimo).length;
        },
        error: () => { this.sinDatos = true; }
      });
  }
}
