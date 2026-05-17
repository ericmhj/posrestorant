import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteFiltro } from './reporte.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-exportar-reporte',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="export-bar">
      <button (click)="exportCSV()" class="btn-export csv">📥 CSV</button>
      <button (click)="exportPDF()" class="btn-export pdf">📄 PDF</button>
    </div>
  `,
  styles: [`
    .export-bar { display: flex; gap: .5rem; }
    .btn-export { padding: .4rem .8rem; border: none; border-radius: 4px; cursor: pointer; font-size: .85rem; }
    .csv { background: #27ae60; color: #fff; }
    .pdf { background: #8e44ad; color: #fff; }
  `]
})
export class ExportarReporteComponent {
  @Input() tipo = 'ventas';
  @Input() filtro!: ReporteFiltro;

  exportCSV(): void {
    if (!this.filtro) return;
    const params = this.buildParams();
    window.open(`${environment.apiUrl}/api/v1/reportes/${this.tipo}/export?${params}`);
  }

  exportPDF(): void {
    // Usa la impresión del navegador como fallback para PDF
    window.print();
  }

  private buildParams(): string {
    const p = new URLSearchParams({
      desde: this.filtro.desde,
      hasta: this.filtro.hasta
    });
    const token = sessionStorage.getItem('pos_token');
    if (token) p.set('token', token);
    if (this.filtro.turno) p.set('turno', this.filtro.turno);
    if (this.filtro.meseroId) p.set('meseroId', this.filtro.meseroId);
    if (this.filtro.estacion) p.set('estacion', this.filtro.estacion);
    if (this.filtro.categoriaId) p.set('categoriaId', this.filtro.categoriaId);
    return p.toString();
  }
}
