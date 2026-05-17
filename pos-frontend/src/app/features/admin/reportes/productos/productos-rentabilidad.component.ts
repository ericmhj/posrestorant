import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ReporteFiltro } from '../shared/reporte.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-productos-rentabilidad',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="productos.length > 0" class="reporte-card">
      <table class="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Producto</th>
            <th>Cant.</th>
            <th>Ingresos</th>
            <th>Costo Unit.</th>
            <th>Costo Total</th>
            <th>Margen $</th>
            <th>Margen %</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let p of productos" [ngClass]="getMargenClass(p.margenPorcentaje)">
            <td>{{ p.ranking }}</td>
            <td>{{ p.nombre }} <small class="cat">{{ p.categoria }}</small></td>
            <td>{{ p.cantidadVendida }}</td>
            <td>$ {{ p.ingresosBrutos | number:'1.2-2' }}</td>
            <td>$ {{ p.costoUnitario | number:'1.2-2' }}</td>
            <td>$ {{ p.costoTotal | number:'1.2-2' }}</td>
            <td>$ {{ p.margenBruto | number:'1.2-2' }}</td>
            <td>
              <span class="margen-badge" [ngClass]="getMargenClass(p.margenPorcentaje)">
                {{ p.margenPorcentaje | number:'1.0-0' }}%
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="leyenda">
        <span class="leyenda-item"><span class="dot rojo"></span> &lt; 20%</span>
        <span class="leyenda-item"><span class="dot amarillo"></span> 20-40%</span>
        <span class="leyenda-item"><span class="dot verde"></span> &gt; 40%</span>
      </div>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">
      No hay datos de rentabilidad. Asegúrate de vincular ingredientes con costo a los productos.
    </div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; font-size: .85rem; }
    .table th { background: #f0f0f0; }
    .cat { color: #888; display: block; font-size: .75rem; }
    .margen-badge { padding: .15rem .5rem; border-radius: 8px; font-weight: 600; font-size: .8rem; }
    .rojo { background: #fdf2f2; color: #e74c3c; }
    .amarillo { background: #fef9e7; color: #d68910; }
    .verde { background: #d5f5e3; color: #27ae60; }
    .leyenda { display: flex; gap: 1rem; margin-top: .75rem; font-size: .8rem; color: #666; }
    .leyenda-item { display: flex; align-items: center; gap: .3rem; }
    .dot { width: 10px; height: 10px; border-radius: 50%; }
    .dot.rojo { background: #e74c3c; }
    .dot.amarillo { background: #d68910; }
    .dot.verde { background: #27ae60; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
  `]
})
export class ProductosRentabilidadComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  productos: any[] = [];
  sinDatos = false;

  constructor(private http: HttpClient) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    let params = new HttpParams()
      .set('desde', this.filtro.desde)
      .set('hasta', this.filtro.hasta);
    if (this.filtro.estacion) params = params.set('estacion', this.filtro.estacion);
    if (this.filtro.categoriaId) params = params.set('categoriaId', this.filtro.categoriaId);

    this.http.get<any[]>(`${environment.apiUrl}/api/v1/reportes/rentabilidad`, { params })
      .subscribe({
        next: (r) => {
          this.productos = r || [];
          this.sinDatos = this.productos.length === 0;
        },
        error: () => { this.sinDatos = true; }
      });
  }

  getMargenClass(margen: number): string {
    if (margen < 20) return 'rojo';
    if (margen < 40) return 'amarillo';
    return 'verde';
  }
}
