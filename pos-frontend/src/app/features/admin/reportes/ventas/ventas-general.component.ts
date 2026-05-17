import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService, ReporteFiltro } from '../shared/reporte.service';
import { ChartReporteComponent, ChartData } from '../shared/chart-reporte.component';

@Component({
  selector: 'app-ventas-general',
  standalone: true,
  imports: [CommonModule, ChartReporteComponent],
  template: `
    <div *ngIf="reporte" class="reporte-card">
      <div class="kpis">
        <div class="kpi">
          <span class="kpi-label">Total Ventas</span>
          <span class="kpi-value">$ {{ reporte.totalVentas | number:'1.2-2' }}</span>
        </div>
        <div class="kpi">
          <span class="kpi-label">Cuentas</span>
          <span class="kpi-value">{{ reporte.numeroCuentas }}</span>
        </div>
        <div class="kpi">
          <span class="kpi-label">Ticket Promedio</span>
          <span class="kpi-value">$ {{ reporte.ticketPromedio | number:'1.2-2' }}</span>
        </div>
      </div>
      <div *ngIf="desglose.length > 0" class="desglose">
        <h4>Desglose por método de pago</h4>
        <app-chart-reporte
          tipo="donut"
          [data]="chartDesglose"
          totalLabel="Cuentas"
          prefijo="$">
        </app-chart-reporte>
      </div>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">No hay ventas en el período seleccionado.</div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .kpis { display: flex; gap: 2rem; flex-wrap: wrap; margin-bottom: 1rem; }
    .kpi { display: flex; flex-direction: column; gap: .2rem; }
    .kpi-label { font-size: .8rem; color: #888; text-transform: uppercase; }
    .kpi-value { font-size: 1.5rem; font-weight: 700; color: #2c3e50; }
    .desglose { border-top: 1px solid #ddd; padding-top: .75rem; }
    .desglose h4 { margin: 0 0 .5rem; font-size: .9rem; color: #555; }
    .desglose-row { display: flex; justify-content: space-between; padding: .25rem 0; font-size: .9rem; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
  `]
})
export class VentasGeneralComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  reporte: any = null;
  desglose: { key: string; value: number }[] = [];
  chartDesglose: ChartData[] = [];
  sinDatos = false;

  constructor(private reporteService: ReporteService) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    this.reporteService.getVentas(this.filtro).subscribe({
      next: (r) => {
        this.reporte = r;
        this.sinDatos = !r || r.numeroCuentas === 0;
        this.desglose = r?.desglosePago
          ? Object.entries(r.desglosePago).map(([key, value]) => ({ key, value: value as number }))
          : [];
        this.chartDesglose = this.desglose.map(d => ({ label: d.key, value: d.value }));
      },
      error: () => { this.sinDatos = true; }
    });
  }
}
