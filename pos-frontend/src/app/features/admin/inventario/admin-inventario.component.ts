import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { LoadingOverlayComponent } from '../../../shared/components/loading-overlay/loading-overlay.component';

@Component({
  selector: 'app-admin-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingOverlayComponent],
  template: `
    <app-loading-overlay [visible]="loading"></app-loading-overlay>
    <div class="admin-container">
      <h2>Control de Inventario</h2>
      <button (click)="toggleForm()" class="btn-primary">
        {{ showForm ? 'Cancelar' : '+ Nuevo Item' }}
      </button>

      <!-- Formulario nuevo item -->
      <div *ngIf="showForm" class="form-card">
        <h3>Nuevo Item de Inventario</h3>
        <label>Nombre *
          <input placeholder="Ej: Pollo, Aceite, Harina" [(ngModel)]="newForm.nombre" />
        </label>
        <label>Categoría
          <select [(ngModel)]="newForm.categoriaId">
            <option value="">-- Sin categoría --</option>
            <option *ngFor="let c of categorias" [value]="c.id">{{ c.nombre }}</option>
          </select>
        </label>
        <label>Unidad de medida
          <input placeholder="Ej: kg, lt, pz, caja" [(ngModel)]="newForm.unidadMedida" />
        </label>
        <label>Stock inicial (cantidad actual en almacén)
          <input type="number" [(ngModel)]="newForm.stockActual" min="0" step="0.001" />
        </label>
        <label>Stock mínimo (alerta cuando baje de este valor)
          <input type="number" [(ngModel)]="newForm.stockMinimo" min="0" step="0.001" />
        </label>
        <p *ngIf="errorMsg" style="color:red;font-size:.85rem">{{ errorMsg }}</p>
        <button (click)="saveNew()" class="btn-primary">Guardar</button>
      </div>

      <!-- Tabs de filtro por categoría -->
      <div class="categoria-tabs">
        <button (click)="categoriaFiltro = ''"
                [class.active]="categoriaFiltro === ''"
                class="tab-btn">
          Todas ({{ items.length }})
        </button>
        <button *ngFor="let c of categorias"
                (click)="categoriaFiltro = c.id"
                [class.active]="categoriaFiltro === c.id"
                class="tab-btn">
          {{ c.nombre }} ({{ countByCategoria(c.id) }})
        </button>
        <button (click)="categoriaFiltro = 'sin'"
                [class.active]="categoriaFiltro === 'sin'"
                class="tab-btn">
          Sin categoría ({{ countSinCategoria() }})
        </button>
      </div>

      <!-- Tabla -->
      <table class="table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Unidad</th>
            <th>Stock</th>
            <th>Mín</th>
            <th>Disponible</th>
            <th>Alerta</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let item of itemsFiltrados()" [class.alerta]="item.alertaMinimo">
            <td>{{ item.nombre }}</td>
            <td>
              <span class="cat-badge" *ngIf="item.categoriaNombre">{{ item.categoriaNombre }}</span>
              <span class="cat-none" *ngIf="!item.categoriaNombre">—</span>
            </td>
            <td>{{ item.unidadMedida }}</td>
            <td>{{ item.stockActual }}</td>
            <td>{{ item.stockMinimo }}</td>
            <td>{{ item.stockDisponible }}</td>
            <td>{{ item.alertaMinimo ? '⚠️' : '' }}</td>
            <td>
              <button (click)="openMovimiento(item)">Movimiento</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Formulario movimiento -->
      <div *ngIf="selectedItem" class="form-card">
        <h3>Registrar Movimiento — {{ selectedItem.nombre }}</h3>
        <select [(ngModel)]="movForm.tipo">
          <option value="ENTRADA">Entrada</option>
          <option value="AJUSTE">Ajuste</option>
          <option value="MERMA">Merma</option>
        </select>
        <input type="number" placeholder="Cantidad" [(ngModel)]="movForm.cantidad" />
        <input placeholder="Motivo (requerido para ajuste/merma)" [(ngModel)]="movForm.motivo" />
        <button (click)="saveMovimiento()" class="btn-primary">Registrar</button>
        <button (click)="selectedItem = null">Cancelar</button>
      </div>
    </div>
  `,
  styles: [`
    .admin-container { padding: 1rem; }
    .table { width: 100%; border-collapse: collapse; margin-top: .75rem; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; font-size: .9rem; }
    .table tr.alerta { background: #fdf2f2; }
    .form-card { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin-top: 1rem; display: flex; flex-direction: column; gap: .5rem; max-width: 420px; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    input, select { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: .25rem .5rem; cursor: pointer; }
    label { display: flex; flex-direction: column; gap: .2rem; font-size: .85rem; color: #555; font-weight: 500; }
    label input, label select { font-weight: normal; }
    /* Tabs */
    .categoria-tabs { display: flex; flex-wrap: wrap; gap: .4rem; margin-top: 1rem; }
    .tab-btn { padding: .3rem .75rem; border: 1px solid #ccc; border-radius: 12px; background: #fff; cursor: pointer; font-size: .85rem; }
    .tab-btn.active { background: #2980b9; color: #fff; border-color: #2980b9; }
    /* Badges */
    .cat-badge { background: #eaf4fb; color: #2980b9; padding: .15rem .5rem; border-radius: 8px; font-size: .8rem; }
    .cat-none { color: #bbb; font-size: .85rem; }
  `]
})
export class AdminInventarioComponent implements OnInit {
  items: any[] = [];
  categorias: any[] = [];
  selectedItem: any = null;
  showForm = false;
  errorMsg = '';
  categoriaFiltro = '';
  newForm: any = { nombre: '', unidadMedida: '', stockActual: 0, stockMinimo: 0, categoriaId: '' };
  movForm = { tipo: 'ENTRADA', cantidad: 0, motivo: '' };
  loading = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.load();
    this.loadCategorias();
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
    if (this.showForm) {
      this.newForm = { nombre: '', unidadMedida: '', stockActual: 0, stockMinimo: 0, categoriaId: '' };
      this.errorMsg = '';
    }
  }

  load(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/inventario`)
      .subscribe(i => {
        this.items = i.sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.loading = false;
      });
  }

  loadCategorias(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/categorias`)
      .subscribe(c => this.categorias = c.sort((a, b) => a.nombre.localeCompare(b.nombre)));
  }

  itemsFiltrados(): any[] {
    if (!this.categoriaFiltro) return this.items;
    if (this.categoriaFiltro === 'sin') return this.items.filter(i => !i.categoriaId);
    return this.items.filter(i => i.categoriaId === this.categoriaFiltro);
  }

  countByCategoria(catId: string): number {
    return this.items.filter(i => i.categoriaId === catId).length;
  }

  countSinCategoria(): number {
    return this.items.filter(i => !i.categoriaId).length;
  }

  saveNew(): void {
    this.errorMsg = '';
    this.loading = true;
    const body = { ...this.newForm };
    if (!body.categoriaId) delete body.categoriaId;
    this.http.post(`${environment.apiUrl}/api/v1/inventario`, body)
      .subscribe({
        next: () => { this.showForm = false; this.load(); },
        error: (e) => { this.loading = false; this.errorMsg = e?.error?.message || 'Error al guardar'; }
      });
  }

  openMovimiento(item: any): void {
    this.selectedItem = item;
    this.movForm = { tipo: 'ENTRADA', cantidad: 0, motivo: '' };
  }

  saveMovimiento(): void {
    this.http.post(
      `${environment.apiUrl}/api/v1/inventario/${this.selectedItem.id}/movimientos`,
      this.movForm
    ).subscribe(() => { this.selectedItem = null; this.load(); });
  }
}
