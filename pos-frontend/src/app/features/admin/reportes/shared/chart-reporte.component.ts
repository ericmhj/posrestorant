import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ChartData {
  label: string;
  value: number;
  color?: string;
}

@Component({
  selector: 'app-chart-reporte',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Barras horizontales -->
    <div *ngIf="tipo === 'barras'" class="chart-barras">
      <div *ngFor="let item of data" class="barra-row">
        <span class="barra-label">{{ item.label }}</span>
        <div class="barra-track">
          <div class="barra-fill"
               [style.width.%]="getPercent(item.value)"
               [style.background]="item.color || defaultColor">
          </div>
        </div>
        <span class="barra-value">{{ formatValue(item.value) }}</span>
      </div>
    </div>

    <!-- Donut -->
    <div *ngIf="tipo === 'donut'" class="chart-donut-container">
      <div class="donut">
        <svg viewBox="0 0 36 36" class="donut-svg">
          <circle *ngFor="let seg of segments; let i = index"
                  class="donut-segment"
                  [attr.stroke]="seg.color"
                  [attr.stroke-dasharray]="seg.dashArray"
                  [attr.stroke-dashoffset]="seg.dashOffset"
                  cx="18" cy="18" r="15.9155"
                  fill="none" stroke-width="3.5">
          </circle>
        </svg>
        <div class="donut-center">
          <span class="donut-total">{{ total | number:'1.0-0' }}</span>
          <span class="donut-label">{{ totalLabel }}</span>
        </div>
      </div>
      <div class="donut-legend">
        <div *ngFor="let item of data" class="legend-item">
          <span class="legend-dot" [style.background]="item.color || defaultColor"></span>
          <span>{{ item.label }}: {{ formatValue(item.value) }}</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    /* Barras */
    .chart-barras { display: flex; flex-direction: column; gap: .5rem; }
    .barra-row { display: flex; align-items: center; gap: .5rem; }
    .barra-label { width: 100px; font-size: .8rem; color: #555; text-align: right; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .barra-track { flex: 1; height: 20px; background: #f0f0f0; border-radius: 4px; overflow: hidden; }
    .barra-fill { height: 100%; border-radius: 4px; transition: width .5s ease; min-width: 2px; }
    .barra-value { width: 70px; font-size: .8rem; color: #333; font-weight: 600; }

    /* Donut */
    .chart-donut-container { display: flex; align-items: center; gap: 1.5rem; }
    .donut { position: relative; width: 140px; height: 140px; }
    .donut-svg { width: 100%; height: 100%; transform: rotate(-90deg); }
    .donut-segment { transition: stroke-dasharray .5s ease; }
    .donut-center { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); text-align: center; }
    .donut-total { display: block; font-size: 1.3rem; font-weight: 700; color: #2c3e50; }
    .donut-label { display: block; font-size: .7rem; color: #888; }
    .donut-legend { display: flex; flex-direction: column; gap: .4rem; }
    .legend-item { display: flex; align-items: center; gap: .4rem; font-size: .85rem; }
    .legend-dot { width: 10px; height: 10px; border-radius: 50%; }
  `]
})
export class ChartReporteComponent implements OnChanges {
  @Input() tipo: 'barras' | 'donut' = 'barras';
  @Input() data: ChartData[] = [];
  @Input() totalLabel = 'Total';
  @Input() prefijo = '';

  defaultColor = '#3498db';
  segments: any[] = [];
  total = 0;
  private maxValue = 1;
  private colors = ['#3498db', '#e74c3c', '#27ae60', '#f39c12', '#8e44ad', '#1abc9c', '#e67e22', '#2c3e50'];

  ngOnChanges(): void {
    this.maxValue = Math.max(...this.data.map(d => d.value), 1);
    this.total = this.data.reduce((sum, d) => sum + d.value, 0);

    // Asignar colores si no tienen
    this.data.forEach((d, i) => {
      if (!d.color) d.color = this.colors[i % this.colors.length];
    });

    // Calcular segmentos del donut
    if (this.tipo === 'donut' && this.total > 0) {
      let offset = 0;
      this.segments = this.data.map(d => {
        const percent = (d.value / this.total) * 100;
        const seg = {
          color: d.color,
          dashArray: `${percent} ${100 - percent}`,
          dashOffset: `${-offset}`
        };
        offset += percent;
        return seg;
      });
    }
  }

  getPercent(value: number): number {
    return (value / this.maxValue) * 100;
  }

  formatValue(value: number): string {
    if (this.prefijo) return `${this.prefijo}${value.toLocaleString('es-MX', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;
    return value.toLocaleString('es-MX');
  }
}
