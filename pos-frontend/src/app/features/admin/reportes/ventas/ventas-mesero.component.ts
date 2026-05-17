import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ReporteFiltro } from '../shared/reporte.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-ventas-mesero',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="meseros.length > 0" class="reporte-card">
      <table class="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Mesero</th>
            <th>Cuentas</th>
            <th>Ingresos</th>
            <th>Ticket Prom.</th>
            <th>Tiempo Prom. (min)</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let m of meseros; let i = index">
            <td>{{ i + 1 }}</td>
            <td>{{ m.nombre }} {{ m.apellido }}</td>
            <td>{{ m.numeroCuentas }}</td>
            <td>$ {{ m.ingresosTotales | number:'1.2-2' }}</td>
            <td>$ {{ m.ticketPromedio | number:'1.2-2' }}</td>
            <td>{{ m.tiempoPromedioCuentaMinutos | number:'1.0-0' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div *ngIf="sinDatos" class="sin-datos">No hay datos de meseros en el período seleccionado.</div>
  `,
  styles: [`
    .reporte-card { background: #f9f9f9; border-radius: 8px; padding: 1.25rem; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; font-size: .9rem; }
    .table th { background: #f0f0f0; }
    .sin-datos { text-align: center; color: #999; padding: 2rem; }
  `]
})
export class VentasMeseroComponent implements OnChanges {
  @Input() filtro!: ReporteFiltro;

  meseros: any[] = [];
  sinDatos = false;

  constructor(private http: HttpClient) {}

  ngOnChanges(): void {
    if (!this.filtro) return;
    let params = new HttpParams()
      .set('desde', this.filtro.desde)
      .set('hasta', this.filtro.hasta);
    if (this.filtro.turno) params = params.set('turno', this.filtro.turno);
    if (this.filtro.meseroId) params = params.set('meseroId', this.filtro.meseroId);

    this.http.get<any[]>(`${environment.apiUrl}/api/v1/reportes/meseros`, { params })
      .subscribe({
        next: (r) => {
          this.meseros = r || [];
          this.sinDatos = this.meseros.length === 0;
        },
        error: () => { this.sinDatos = true; }
      });
  }
}
