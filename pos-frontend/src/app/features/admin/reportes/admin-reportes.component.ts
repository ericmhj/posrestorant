import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FiltroReporteComponent } from './shared/filtro-reporte.component';
import { VentasGeneralComponent } from './ventas/ventas-general.component';
import { VentasMeseroComponent } from './ventas/ventas-mesero.component';
import { ProductosVendidosComponent } from './productos/productos-vendidos.component';
import { ProductosRentabilidadComponent } from './productos/productos-rentabilidad.component';
import { HorasPicoComponent } from './operacion/horas-pico.component';
import { EstacionesComponent } from './operacion/estaciones.component';
import { InventarioDetalladoComponent } from './inventario/inventario-detallado.component';
import { ExportarReporteComponent } from './shared/exportar-reporte.component';
import { ReporteFiltro } from './shared/reporte.service';
import { LoadingOverlayComponent } from '../../../shared/components/loading-overlay/loading-overlay.component';

@Component({
  selector: 'app-admin-reportes',
  standalone: true,
  imports: [
    CommonModule,
    LoadingOverlayComponent,
    FiltroReporteComponent,
    VentasGeneralComponent,
    VentasMeseroComponent,
    ProductosVendidosComponent,
    ProductosRentabilidadComponent,
    HorasPicoComponent,
    EstacionesComponent,
    InventarioDetalladoComponent,
    ExportarReporteComponent
  ],
  template: `
    <app-loading-overlay [visible]="loading"></app-loading-overlay>
    <div class="reportes-container">
      <h2>📊 Reportes</h2>

      <app-filtro-reporte (onFilter)="aplicarFiltro($event)"></app-filtro-reporte>

      <!-- Tabs -->
      <div class="tabs" *ngIf="filtroActivo">
        <button *ngFor="let tab of tabs"
                (click)="tabActivo = tab.id"
                [class.active]="tabActivo === tab.id"
                class="tab-btn">
          {{ tab.icono }} {{ tab.nombre }}
        </button>
        <button (click)="exportar()" class="btn-export">📥 Exportar CSV</button>
        <app-exportar-reporte *ngIf="filtroActivo" [tipo]="tabActivo" [filtro]="filtroActivo"></app-exportar-reporte>
      </div>

      <!-- Contenido del tab activo -->
      <div *ngIf="filtroActivo" class="tab-content">
        <app-ventas-general
          *ngIf="tabActivo === 'ventas'"
          [filtro]="filtroActivo">
        </app-ventas-general>

        <app-productos-vendidos
          *ngIf="tabActivo === 'productos'"
          [filtro]="filtroActivo">
        </app-productos-vendidos>

        <app-ventas-mesero
          *ngIf="tabActivo === 'meseros'"
          [filtro]="filtroActivo">
        </app-ventas-mesero>

        <app-horas-pico
          *ngIf="tabActivo === 'horas-pico'"
          [filtro]="filtroActivo">
        </app-horas-pico>

        <app-productos-rentabilidad
          *ngIf="tabActivo === 'rentabilidad'"
          [filtro]="filtroActivo">
        </app-productos-rentabilidad>

        <app-estaciones
          *ngIf="tabActivo === 'estaciones'"
          [filtro]="filtroActivo">
        </app-estaciones>

        <app-inventario-detallado
          *ngIf="tabActivo === 'inventario'"
          [filtro]="filtroActivo">
        </app-inventario-detallado>
      </div>

      <!-- Placeholder -->
      <div *ngIf="!filtroActivo" class="placeholder">
        Selecciona un período y haz clic en <strong>🔍 Aplicar</strong> para ver los reportes.
      </div>
    </div>
  `,
  styles: [`
    .reportes-container { padding: 1rem; max-width: 1000px; }
    .tabs { display: flex; gap: .4rem; flex-wrap: wrap; margin-bottom: 1rem; align-items: center; }
    .tab-btn { padding: .4rem .9rem; border: 1px solid #ccc; border-radius: 8px; background: #fff; cursor: pointer; font-size: .9rem; }
    .tab-btn.active { background: #2980b9; color: #fff; border-color: #2980b9; }
    .btn-export { margin-left: auto; padding: .4rem .9rem; background: #27ae60; color: #fff; border: none; border-radius: 8px; cursor: pointer; font-size: .9rem; }
    .tab-content { margin-top: .5rem; }
    .placeholder { text-align: center; color: #999; padding: 3rem; background: #f8f9fa; border-radius: 8px; border: 2px dashed #e0e0e0; }
  `]
})
export class AdminReportesComponent {
  filtroActivo: ReporteFiltro | null = null;
  tabActivo = 'ventas';
  loading = false;

  tabs = [
    { id: 'ventas', nombre: 'Ventas', icono: '💰' },
    { id: 'productos', nombre: 'Productos', icono: '🍔' },
    { id: 'rentabilidad', nombre: 'Rentabilidad', icono: '📈' },
    { id: 'meseros', nombre: 'Meseros', icono: '👤' },
    { id: 'horas-pico', nombre: 'Horas Pico', icono: '⏰' },
    { id: 'estaciones', nombre: 'Estaciones', icono: '🍳' },
    { id: 'inventario', nombre: 'Inventario', icono: '📦' }
  ];

  aplicarFiltro(filtro: ReporteFiltro): void {
    this.filtroActivo = { ...filtro };
  }

  exportar(): void {
    if (!this.filtroActivo) return;
    const token = sessionStorage.getItem('pos_token');
    const params = new URLSearchParams({
      desde: this.filtroActivo.desde,
      hasta: this.filtroActivo.hasta
    });
    if (this.filtroActivo.turno) params.set('turno', this.filtroActivo.turno);
    if (this.filtroActivo.meseroId) params.set('meseroId', this.filtroActivo.meseroId);
    if (this.filtroActivo.estacion) params.set('estacion', this.filtroActivo.estacion);
    if (this.filtroActivo.categoriaId) params.set('categoriaId', this.filtroActivo.categoriaId);
    if (token) params.set('token', token);

    window.open(`/api/v1/reportes/${this.tabActivo}/export?${params.toString()}`);
  }
}
