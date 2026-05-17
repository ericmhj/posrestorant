import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReporteService, ReporteFiltro } from '../shared/reporte.service';

@Component({
  selector: 'app-productos-vendidos',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="productos.length > 0" class="reporte-card">
      <table class="table">
        <thead>
          <tr><th>#</th><th>Producto</th><th>Cantidad</th><th>Ingresos</th><th>Categoría</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let p of productos; let i = index">
            <td>{{ i + 1 }}</td>
            <td>{{ p.nombre }}</td>
            <td>{{ p.cantidadVendida }}</td>
            <td>$ {{ p.ingresos | number:'1.2-2' }}</td>
            <td>{{ p.categoria || '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">No hay productos vendidos en el período seleccionado.</div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; font-size: .9rem; }
    .table th { background: #f0f0f0; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
  `]
})
export class ProductosVendidosComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  productos: any[] = [];
  sinDatos = false;

  constructor(private reporteService: ReporteService) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    this.reporteService.getProductos(this.filtro).subscribe({
      next: (r) => {
        this.productos = r || [];
        this.sinDatos = this.productos.length === 0;
      },
      error: () => { this.sinDatos = true; }
    });
  }
}
