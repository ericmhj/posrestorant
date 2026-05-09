import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ProductPlaceholderComponent } from '../../../shared/components/product-placeholder';
import { LoadingOverlayComponent } from '../../../shared/components/loading-overlay/loading-overlay.component';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductPlaceholderComponent, LoadingOverlayComponent],
  template: `
    <app-loading-overlay [visible]="loading"></app-loading-overlay>
    <div class="admin-container">
      <h2>Gestión de Productos</h2>
      <button (click)="toggleForm()" class="btn-primary">
        {{ showForm ? 'Cancelar' : '+ Nuevo Producto' }}
      </button>

      <!-- Formulario nuevo/editar producto -->
      <div *ngIf="showForm" class="form-card">
        <h3>{{ editId ? 'Editar' : 'Nuevo' }} Producto</h3>
        <input placeholder="Nombre" [(ngModel)]="form.nombre" />
        <input placeholder="Descripción" [(ngModel)]="form.descripcion" />
        <input type="number" placeholder="Precio" [(ngModel)]="form.precio" />
        <select [(ngModel)]="form.estacion">
          <option value="COCINA">Cocina</option>
          <option value="BARRA">Barra</option>
        </select>
        <select [(ngModel)]="form.categoriaId">
          <option value="">-- Selecciona categoría --</option>
          <option *ngFor="let c of categorias" [value]="c.id">{{ c.nombre }}</option>
        </select>
        <p *ngIf="errorMsg" style="color:red;font-size:.85rem">{{ errorMsg }}</p>
        <button (click)="save()" class="btn-primary">Guardar</button>
      </div>

      <div class="layout">
        <!-- Grid de productos -->
        <div class="productos-grid">
          <div *ngFor="let p of productos" class="producto-card"
               [class.selected]="selectedProducto?.id === p.id">
            <app-product-placeholder
              [productName]="p.nombre"
              [imagenUrl]="p.imagenUrl"
              size="80px">
            </app-product-placeholder>
            <div class="producto-info">
              <strong>{{ p.nombre }}</strong>
              <span>$ {{ p.precio }}</span>
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
              <button (click)="openIngredientes(p)" class="btn-ingredientes">🧪 Ingredientes</button>
            </div>
          </div>
        </div>

        <!-- Panel de ingredientes -->
        <div *ngIf="selectedProducto" class="ingredientes-panel">
          <div class="panel-header">
            <h3>Ingredientes — {{ selectedProducto.nombre }}</h3>
            <button (click)="selectedProducto = null" class="btn-close">✕</button>
          </div>

          <!-- Lista de ingredientes vinculados -->
          <div *ngIf="ingredientes.length === 0" class="empty-msg">
            Sin ingredientes vinculados
          </div>
          <table *ngIf="ingredientes.length > 0" class="ing-table">
            <thead>
              <tr><th>Ingrediente</th><th>Cantidad</th><th>Unidad</th><th></th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let ing of ingredientes">
                <td>{{ ing.itemInventarioNombre }}</td>
                <td>{{ ing.cantidad }}</td>
                <td>{{ ing.unidadMedida }}</td>
                <td>
                  <button (click)="removeIngrediente(ing.id)" class="btn-danger">🗑</button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Formulario agregar ingrediente -->
          <div class="add-ing-form">
            <h4>Agregar ingredientes</h4>
            <small style="color:#666">Mantén <kbd>Ctrl</kbd> para seleccionar varios</small>
            <select multiple size="6" (change)="onItemsSelected($event)" class="multi-select" #multiSelect>
              <option *ngFor="let item of itemsInventario" [value]="item.id">
                {{ item.nombre }} ({{ item.unidadMedida }})
              </option>
            </select>
            <label style="font-size:.85rem;color:#555">
              Cantidad por unidad vendida
              <input type="number" [(ngModel)]="ingForm.cantidad" step="0.001" min="0.001" />
            </label>
            <p *ngIf="ingErrorMsg" style="color:red;font-size:.85rem">{{ ingErrorMsg }}</p>
            <button (click)="addIngredientes()" class="btn-primary"
                    [disabled]="selectedItemIds.length === 0">
              Agregar {{ selectedItemIds.length > 1 ? '(' + selectedItemIds.length + ' items)' : '' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-container { padding: 1rem; }
    .form-card { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin: 1rem 0; display: flex; flex-direction: column; gap: .5rem; max-width: 400px; }
    .layout { display: flex; gap: 1.5rem; align-items: flex-start; margin-top: 1rem; }
    .productos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1rem; flex: 1; }
    .producto-card { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: .5rem; }
    .producto-card.selected { border-color: #2980b9; box-shadow: 0 0 0 2px #2980b933; }
    .producto-info { display: flex; flex-direction: column; gap: .2rem; }
    .badge { font-size: .75rem; padding: .2rem .5rem; border-radius: 4px; background: #eee; }
    .badge.activo { background: #d5f5e3; color: #27ae60; }
    .producto-actions { display: flex; flex-wrap: wrap; gap: .25rem; }
    .producto-actions button, .upload-btn { padding: .25rem .5rem; font-size: .8rem; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: #fff; }
    .btn-ingredientes { background: #8e44ad !important; color: #fff !important; border-color: #8e44ad !important; }
    .btn-primary { padding: .5rem 1rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
    .btn-danger { background: #e74c3c; color: #fff; border: none; border-radius: 4px; cursor: pointer; padding: .2rem .4rem; }
    input, select { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; width: 100%; box-sizing: border-box; }
    /* Ingredientes panel */
    .ingredientes-panel { width: 360px; min-width: 320px; background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: .75rem; position: sticky; top: 1rem; }
    .panel-header { display: flex; justify-content: space-between; align-items: center; }
    .panel-header h3 { margin: 0; font-size: 1rem; }
    .btn-close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #666; }
    .ing-table { width: 100%; border-collapse: collapse; font-size: .85rem; }
    .ing-table th, .ing-table td { padding: .35rem .5rem; border: 1px solid #eee; }
    .ing-table th { background: #f5f5f5; }
    .add-ing-form { display: flex; flex-direction: column; gap: .5rem; border-top: 1px solid #eee; padding-top: .75rem; }
    .add-ing-form h4 { margin: 0; font-size: .9rem; color: #555; }
    .empty-msg { color: #999; font-size: .85rem; text-align: center; padding: .5rem; }
    .multi-select { height: 150px; width: 100%; }
  `]
})
export class AdminProductosComponent implements OnInit {
  productos: any[] = [];
  categorias: any[] = [];
  itemsInventario: any[] = [];
  showForm = false;
  editId: string | null = null;
  errorMsg = '';
  loading = false;
  form = { nombre: '', descripcion: '', precio: 0, estacion: 'COCINA', categoriaId: '' };

  // Ingredientes
  @ViewChild('multiSelect') multiSelectRef!: ElementRef<HTMLSelectElement>;
  selectedProducto: any = null;
  ingredientes: any[] = [];
  selectedItemIds: string[] = [];
  ingForm = { itemInventarioId: '', cantidad: 1 };
  ingErrorMsg = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.load();
    this.loadCategorias();
    this.loadItemsInventario();
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
    if (this.showForm) {
      // Siempre limpia al abrir para nuevo producto
      this.editId = null;
      this.errorMsg = '';
      this.form = { nombre: '', descripcion: '', precio: 0, estacion: 'COCINA', categoriaId: '' };
    }
  }

  load(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/productos?soloActivos=false`)
      .subscribe(p => this.productos = p);
  }

  loadCategorias(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/categorias`)
      .subscribe(c => this.categorias = c);
  }

  loadItemsInventario(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/inventario`)
      .subscribe(i => this.itemsInventario = i);
  }

  save(): void {
    this.errorMsg = '';
    this.loading = true;
    const req = this.editId
      ? this.http.put(`${environment.apiUrl}/api/v1/productos/${this.editId}`, this.form)
      : this.http.post(`${environment.apiUrl}/api/v1/productos`, this.form);
    req.subscribe({
      next: () => { this.loading = false; this.showForm = false; this.editId = null; this.load(); },
      error: (e) => { this.loading = false; this.errorMsg = e?.error?.message || 'Error al guardar'; }
    });
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
    this.loading = true;
    this.http.post(`${environment.apiUrl}/api/v1/productos/${id}/imagen`, fd)
      .subscribe({
        next: () => { this.loading = false; this.load(); },
        error: () => { this.loading = false; }
      });
  }

  deleteImage(id: string): void {
    this.loading = true;
    this.http.delete(`${environment.apiUrl}/api/v1/productos/${id}/imagen`)
      .subscribe({
        next: () => { this.loading = false; this.load(); },
        error: () => { this.loading = false; }
      });
  }

  // -------------------------------------------------------
  // Ingredientes
  // -------------------------------------------------------

  openIngredientes(p: any): void {
    this.selectedProducto = p;
    this.selectedItemIds = [];
    this.ingForm = { itemInventarioId: '', cantidad: 1 };
    this.ingErrorMsg = '';
    this.loadIngredientes(p.id);
  }

  loadIngredientes(productoId: string): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/productos/${productoId}/ingredientes`)
      .subscribe(i => {
        this.ingredientes = i;
        this.loading = false;
      });
  }

  onItemsSelected(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedItemIds = Array.from(select.selectedOptions).map(o => o.value);
  }

  addIngredientes(): void {
    this.ingErrorMsg = '';
    if (this.selectedItemIds.length === 0) {
      this.ingErrorMsg = 'Selecciona al menos un item';
      return;
    }
    if (!this.ingForm.cantidad || this.ingForm.cantidad <= 0) {
      this.ingErrorMsg = 'La cantidad debe ser mayor a 0';
      return;
    }

    // Enviar todos los seleccionados en secuencia
    const requests = this.selectedItemIds.map(id =>
      this.http.post(
        `${environment.apiUrl}/api/v1/productos/${this.selectedProducto.id}/ingredientes`,
        { itemInventarioId: id, cantidad: this.ingForm.cantidad }
      ).toPromise().catch(e => ({ error: e?.error?.message || 'Error' }))
    );

    this.loading = true;
    Promise.all(requests).then(() => {
      this.selectedItemIds = [];
      this.ingForm = { itemInventarioId: '', cantidad: 1 };
      // Deseleccionar todos los items del select
      if (this.multiSelectRef?.nativeElement) {
        Array.from(this.multiSelectRef.nativeElement.options).forEach(o => o.selected = false);
      }
      this.loadIngredientes(this.selectedProducto.id); // loading=false se ejecuta dentro de loadIngredientes
    });
  }

  removeIngrediente(ingredienteId: string): void {
    this.http.delete(
      `${environment.apiUrl}/api/v1/productos/${this.selectedProducto.id}/ingredientes/${ingredienteId}`
    ).subscribe(() => this.loadIngredientes(this.selectedProducto.id));
  }
}
