import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-container">
      <h2>Control de Inventario</h2>
      <button (click)="showForm = !showForm" class="btn-primary">
        {{ showForm ? 'Cancelar' : '+ Nuevo Item' }}
      </button>

      <div *ngIf="showForm" class="form-card">
        <h3>Nuevo Item de Inventario</h3>
        <input placeholder="Nombre" [(ngModel)]="newForm.nombre" />
        <input placeholder="Unidad (kg, lt, pz...)" [(ngModel)]="newForm.unidadMedida" />
        <input type="number" placeholder="Stock inicial" [(ngModel)]="newForm.stockActual" />
        <input type="number" placeholder="Stock mínimo" [(ngModel)]="newForm.stockMinimo" />
        <p *ngIf="errorMsg" style="color:red;font-size:.85rem">{{ errorMsg }}</p>
        <button (click)="saveNew()" class="btn-primary">Guardar</button>
      </div>

      <table class="table">
        <thead>
          <tr><th>Nombre</th><th>Unidad</th><th>Stock</th><th>Mín</th><th>Disponible</th><th>Alerta</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let item of items" [class.alerta]="item.alertaMinimo">
            <td>{{ item.nombre }}</td>
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
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; text-align: left; }
    .table tr.alerta { background: #fdf2f2; }
    .form-card { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin-top: 1rem; display: flex; flex-direction: column; gap: .5rem; max-width: 400px; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    input, select { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: .25rem .5rem; cursor: pointer; }
  `]
})
export class AdminInventarioComponent implements OnInit {
  items: any[] = [];
  selectedItem: any = null;
  showForm = false;
  errorMsg = '';
  newForm = { nombre: '', unidadMedida: '', stockActual: 0, stockMinimo: 0 };
  movForm = { tipo: 'ENTRADA', cantidad: 0, motivo: '' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/inventario`)
      .subscribe(i => this.items = i);
  }

  saveNew(): void {
    this.errorMsg = '';
    this.http.post(`${environment.apiUrl}/api/v1/inventario`, this.newForm)
      .subscribe({
        next: () => {
          this.showForm = false;
          this.newForm = { nombre: '', unidadMedida: '', stockActual: 0, stockMinimo: 0 };
          this.load();
        },
        error: (e) => { this.errorMsg = e?.error?.message || 'Error al guardar'; }
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
