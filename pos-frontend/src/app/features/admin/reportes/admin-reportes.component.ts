import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-container">
      <h2>Reportes</h2>
      <div class="filters">
        <label>Desde: <input type="datetime-local" [(ngModel)]="desde" /></label>
        <label>Hasta: <input type="datetime-local" [(ngModel)]="hasta" /></label>
        <button (click)="loadVentas()" class="btn-primary">Ventas</button>
        <button (click)="loadProductos()" class="btn-primary">Productos</button>
        <button (click)="exportVentas()">Exportar Ventas CSV</button>
      </div>

      <div *ngIf="ventasReporte" class="reporte-card">
        <h3>Reporte de Ventas</h3>
        <p>Total: $ {{ ventasReporte.totalVentas }}</p>
        <p>Cuentas: {{ ventasReporte.numeroCuentas }}</p>
        <p>Ticket Promedio: $ {{ ventasReporte.ticketPromedio }}</p>
      </div>

      <div *ngIf="productosReporte.length > 0" class="reporte-card">
        <h3>Productos más vendidos</h3>
        <table class="table">
          <thead><tr><th>Producto</th><th>Cantidad</th><th>Ingresos</th><th>Categoría</th></tr></thead>
          <tbody>
            <tr *ngFor="let p of productosReporte">
              <td>{{ p.nombre }}</td>
              <td>{{ p.cantidadVendida }}</td>
              <td>$ {{ p.ingresos }}</td>
              <td>{{ p.categoria }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .admin-container { padding: 1rem; }
    .filters { display: flex; gap: 1rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem; }
    .reporte-card { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    button { padding: .5rem 1rem; cursor: pointer; }
    input { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; }
  `]
})
export class AdminReportesComponent implements OnInit {
  desde = '';
  hasta = '';
  ventasReporte: any = null;
  productosReporte: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    const now = new Date();
    const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    this.desde = monthAgo.toISOString().slice(0, 16);
    this.hasta = now.toISOString().slice(0, 16);
  }

  loadVentas(): void {
    this.http.get<any>(`${environment.apiUrl}/api/v1/reportes/ventas?desde=${this.desde}&hasta=${this.hasta}`)
      .subscribe(r => this.ventasReporte = r);
  }

  loadProductos(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/reportes/productos?desde=${this.desde}&hasta=${this.hasta}`)
      .subscribe(r => this.productosReporte = r);
  }

  exportVentas(): void {
    window.open(`${environment.apiUrl}/api/v1/reportes/ventas/export?desde=${this.desde}&hasta=${this.hasta}`);
  }
}
