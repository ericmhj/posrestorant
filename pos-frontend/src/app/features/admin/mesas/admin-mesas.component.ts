import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-mesas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-container">
      <h2>Gestión de Mesas</h2>
      <div class="form-inline">
        <input placeholder="Nombre de mesa" [(ngModel)]="newNombre" />
        <button (click)="create()" class="btn-primary">+ Agregar</button>
      </div>
      <table class="table">
        <thead><tr><th>Nombre</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr *ngFor="let m of mesas">
            <td>
              <span *ngIf="editId !== m.id">{{ m.nombre }}</span>
              <input *ngIf="editId === m.id" [(ngModel)]="editNombre" />
            </td>
            <td>{{ m.estado }}</td>
            <td>
              <button *ngIf="editId !== m.id" (click)="startEdit(m)">Editar</button>
              <button *ngIf="editId === m.id" (click)="saveEdit(m.id)" class="btn-primary">Guardar</button>
              <button *ngIf="editId === m.id" (click)="editId = null">Cancelar</button>
              <button (click)="delete(m.id)" [disabled]="m.estado === 'OCUPADA'">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .admin-container { padding: 1rem; }
    .form-inline { display: flex; gap: .5rem; margin-bottom: 1rem; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    input { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: .25rem .5rem; cursor: pointer; margin-right: .25rem; }
    button:disabled { opacity: .5; cursor: not-allowed; }
  `]
})
export class AdminMesasComponent implements OnInit {
  mesas: any[] = [];
  newNombre = '';
  editId: string | null = null;
  editNombre = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/mesas`)
      .subscribe(m => this.mesas = m);
  }

  create(): void {
    if (!this.newNombre.trim()) return;
    this.http.post(`${environment.apiUrl}/api/v1/mesas`, { nombre: this.newNombre })
      .subscribe(() => { this.newNombre = ''; this.load(); });
  }

  startEdit(m: any): void { this.editId = m.id; this.editNombre = m.nombre; }

  saveEdit(id: string): void {
    this.http.put(`${environment.apiUrl}/api/v1/mesas/${id}`, { nombre: this.editNombre })
      .subscribe(() => { this.editId = null; this.load(); });
  }

  delete(id: string): void {
    this.http.delete(`${environment.apiUrl}/api/v1/mesas/${id}`)
      .subscribe(() => this.load());
  }
}
