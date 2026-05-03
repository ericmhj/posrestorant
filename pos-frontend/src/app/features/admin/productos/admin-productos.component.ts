import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ProductPlaceholderComponent } from '../../../shared/components/product-placeholder';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductPlaceholderComponent],
  template: `
    <div class="admin-container">
      <h2>Gestión de Productos</h2>
      <button (click)="showForm = !showForm" class="btn-primary">
        {{ showForm ? 'Cancelar' : '+ Nuevo Producto' }}
      </button>

      <div *ngIf="showForm" class="form-card">
        <h3>{{ editId ? 'Editar' : 'Nuevo' }} Producto</h3>
        <input placeholder="Nombre" [(ngModel)]="form.nombre" />
        <input placeholder="Descripción" [(ngModel)]="form.descripcion" />
        <input type="number" placeholder="Precio" [(ngModel)]="form.precio" />
        <select [(ngModel)]="form.estacion">
          <option value="COCINA">Cocina</option>
          <option value="BARRA">Barra</option>
        </select>
        <button (click)="save()" class="btn-primary">Guardar</button>
      </div>

      <div class="productos-grid">
        <div *ngFor="let p of productos" class="producto-card">
          <app-product-placeholder
            [productName]="p.nombre"
            [imagenUrl]="p.imagenUrl"
            size="80px">
          </app-product-placeholder>
          <div class="producto-info">
            <strong>{{ p.nombre }}</strong>
            <span>${{ p.precio }}</span>
            <span class="badge" [class.activo]="p.activo">{{ p.activo ? 'Activo' : 'Inactivo' }}</span>
          </div>
          <div class="producto-actions">
            <button (click)="edit(p)">Editar</button>
            <button (click)="deactivate(p.id)" *ngIf="p.activo">Desactivar</button>
            <label class="upload-btn">
              📷 Imagen
              <input type="file" accept="image/jpeg,image/png,image/webp"
                     (change)="uploadImage(p.id, $event)" hidden />
            </label>
            <button *ngIf="p.imagenUrl" (click)="deleteImage(p.id)">🗑 Imagen</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-container { padding: 1rem; }
    .form-card { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin: 1rem 0; display: flex; flex-direction: column; gap: .5rem; max-width: 400px; }
    .productos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1rem; margin-top: 1rem; }
    .producto-card { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: .5rem; }
    .producto-info { display: flex; flex-direction: column; gap: .2rem; }
    .badge { font-size: .75rem; padding: .2rem .5rem; border-radius: 4px; background: #eee; }
    .badge.activo { background: #d5f5e3; color: #27ae60; }
    .producto-actions { display: flex; flex-wrap: wrap; gap: .25rem; }
    .producto-actions button, .upload-btn { padding: .25rem .5rem; font-size: .8rem; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: #fff; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    input, select { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; }
  `]
})
export class AdminProductosComponent implements OnInit {
  productos: any[] = [];
  showForm = false;
  editId: string | null = null;
  form = { nombre: '', descripcion: '', precio: 0, estacion: 'COCINA', categoriaId: '' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/productos?soloActivos=false`)
      .subscribe(p => this.productos = p);
  }

  save(): void {
    const req = this.editId
      ? this.http.put(`${environment.apiUrl}/api/v1/productos/${this.editId}`, this.form)
      : this.http.post(`${environment.apiUrl}/api/v1/productos`, this.form);
    req.subscribe(() => { this.showForm = false; this.editId = null; this.load(); });
  }

  edit(p: any): void {
    this.editId = p.id;
    this.form = { nombre: p.nombre, descripcion: p.descripcion, precio: p.precio, estacion: p.estacion, categoriaId: p.categoria?.id };
    this.showForm = true;
  }

  deactivate(id: string): void {
    this.http.delete(`${environment.apiUrl}/api/v1/productos/${id}`)
      .subscribe(() => this.load());
  }

  uploadImage(id: string, event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const fd = new FormData();
    fd.append('imagen', file);
    this.http.post(`${environment.apiUrl}/api/v1/productos/${id}/imagen`, fd)
      .subscribe(() => this.load());
  }

  deleteImage(id: string): void {
    this.http.delete(`${environment.apiUrl}/api/v1/productos/${id}/imagen`)
      .subscribe(() => this.load());
  }
}
