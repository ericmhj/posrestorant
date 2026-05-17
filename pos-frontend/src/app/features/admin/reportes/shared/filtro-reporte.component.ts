import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService, ReporteFiltro } from './reporte.service';

@Component({
  selector: 'app-filtro-reporte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="filtros-bar">
      <div class="filtro-group">
        <label>Desde
          <input type="datetime-local" [(ngModel)]="filtro.desde" />
        </label>
        <label>Hasta
          <input type="datetime-local" [(ngModel)]="filtro.hasta" />
        </label>
      </div>
      <div class="filtro-group">
        <label>Turno
          <select [(ngModel)]="filtro.turno">
            <option value="">Todos</option>
            <option value="ALMUERZO">Almuerzo (11-16h)</option>
            <option value="CENA">Cena (16-22h)</option>
            <option value="NOCHE">Noche (22-04h)</option>
          </select>
        </label>
        <label>Mesero
          <select [(ngModel)]="filtro.meseroId">
            <option value="">Todos</option>
            <option *ngFor="let m of meseros" [value]="m.id">{{ m.nombre }} {{ m.apellido }}</option>
          </select>
        </label>
        <label>Estación
          <select [(ngModel)]="filtro.estacion">
            <option value="">Todas</option>
            <option value="COCINA">Cocina</option>
            <option value="BARRA">Barra</option>
          </select>
        </label>
        <label>Categoría
          <select [(ngModel)]="filtro.categoriaId">
            <option value="">Todas</option>
            <option *ngFor="let c of categorias" [value]="c.id">{{ c.nombre }}</option>
          </select>
        </label>
      </div>
      <button (click)="aplicar()" class="btn-aplicar">🔍 Aplicar</button>
    </div>
  `,
  styles: [`
    .filtros-bar { display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end; padding: .75rem; background: #f8f9fa; border-radius: 8px; margin-bottom: 1.5rem; }
    .filtro-group { display: flex; gap: .75rem; flex-wrap: wrap; }
    label { display: flex; flex-direction: column; gap: .2rem; font-size: .8rem; color: #555; font-weight: 500; }
    input, select { padding: .35rem .5rem; border: 1px solid #ccc; border-radius: 4px; font-size: .85rem; min-width: 130px; }
    .btn-aplicar { padding: .5rem 1.25rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: .9rem; white-space: nowrap; }
  `]
})
export class FiltroReporteComponent implements OnInit {
  @Output() onFilter = new EventEmitter<ReporteFiltro>();

  filtro: ReporteFiltro = { desde: '', hasta: '' };
  meseros: any[] = [];
  categorias: any[] = [];

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    const now = new Date();
    const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    this.filtro.desde = monthAgo.toISOString().slice(0, 16);
    this.filtro.hasta = now.toISOString().slice(0, 16);

    this.reporteService.getMeseros().subscribe(m => this.meseros = m);
    this.reporteService.getCategorias().subscribe(c => this.categorias = c);
  }

  aplicar(): void {
    this.onFilter.emit({ ...this.filtro });
  }
}
