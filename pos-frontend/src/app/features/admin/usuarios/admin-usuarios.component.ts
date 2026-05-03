import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-container">
      <h2>Gestión de Usuarios</h2>
      <button (click)="showForm = !showForm" class="btn-primary">
        {{ showForm ? 'Cancelar' : '+ Nuevo Usuario' }}
      </button>

      <div *ngIf="showForm" class="form-card">
        <input placeholder="Nombre" [(ngModel)]="form.nombre" />
        <input placeholder="Apellido" [(ngModel)]="form.apellido" />
        <input placeholder="Username" [(ngModel)]="form.username" />
        <input type="password" placeholder="Contraseña" [(ngModel)]="form.password" />
        <select [(ngModel)]="form.rol">
          <option value="ADMIN">Admin</option>
          <option value="MESERO">Mesero</option>
          <option value="COCINA">Cocina</option>
          <option value="BARRA">Barra</option>
        </select>
        <button (click)="save()" class="btn-primary">Guardar</button>
      </div>

      <table class="table">
        <thead>
          <tr><th>Nombre</th><th>Username</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let u of usuarios">
            <td>{{ u.nombre }} {{ u.apellido }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.rol }}</td>
            <td>{{ u.activo ? 'Activo' : 'Inactivo' }}</td>
            <td>
              <button *ngIf="u.activo" (click)="deactivate(u.id)">Desactivar</button>
              <button (click)="resetPassword(u.id)">Reset Pass</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .admin-container { padding: 1rem; }
    .form-card { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin: 1rem 0; display: flex; flex-direction: column; gap: .5rem; max-width: 400px; }
    .table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
    .table th, .table td { padding: .5rem; border: 1px solid #ddd; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    input, select { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: .25rem .5rem; cursor: pointer; margin-right: .25rem; }
  `]
})
export class AdminUsuariosComponent implements OnInit {
  usuarios: any[] = [];
  showForm = false;
  form = { nombre: '', apellido: '', username: '', password: '', rol: 'MESERO' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/usuarios`)
      .subscribe(u => this.usuarios = u);
  }

  save(): void {
    this.http.post(`${environment.apiUrl}/api/v1/usuarios`, this.form)
      .subscribe(() => { this.showForm = false; this.load(); });
  }

  deactivate(id: string): void {
    this.http.put(`${environment.apiUrl}/api/v1/usuarios/${id}/desactivar`, {})
      .subscribe(() => this.load());
  }

  resetPassword(id: string): void {
    const newPassword = prompt('Nueva contraseña:');
    if (!newPassword) return;
    this.http.put(`${environment.apiUrl}/api/v1/usuarios/${id}/reset-password`, { newPassword })
      .subscribe(() => alert('Contraseña actualizada'));
  }
}
