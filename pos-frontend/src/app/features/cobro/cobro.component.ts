import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-cobro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="cobro-container">
      <h3>Cobro — {{ mesaNombre }}</h3>

      <div class="resumen">
        <div *ngFor="let item of items" class="item-row">
          <span>{{ item.cantidad }}x {{ item.productoNombre }}</span>
          <span *ngIf="item.modificadores" class="mod">{{ item.modificadores }}</span>
          <span class="precio">$ {{ (item.precioUnitario * item.cantidad) | number:'1.2-2' }}</span>
        </div>
        <hr />
        <div class="total-row">
          <span>Subtotal:</span>
          <span>$ {{ subtotal | number:'1.2-2' }}</span>
        </div>
        <div class="total-row">
          <span>IVA (16%):</span>
          <span>$ {{ impuestos | number:'1.2-2' }}</span>
        </div>
        <div class="total-row total-final">
          <strong>Total:</strong>
          <strong>$ {{ total | number:'1.2-2' }}</strong>
        </div>
      </div>

      <div class="pago-form">
        <label>Método de pago:</label>
        <select [(ngModel)]="metodoPago">
          <option value="EFECTIVO">Efectivo</option>
          <option value="TARJETA_CREDITO">Tarjeta de Crédito</option>
          <option value="TARJETA_DEBITO">Tarjeta de Débito</option>
        </select>

        <div *ngIf="metodoPago === 'EFECTIVO'" class="efectivo-section">
          <label>Monto recibido:</label>
          <input type="number" [(ngModel)]="montoRecibido"
                 [min]="total" step="0.01" />
          <div *ngIf="montoRecibido >= total" class="cambio">
            Cambio: $ {{ (montoRecibido - total) | number:'1.2-2' }}
          </div>
        </div>

        <div *ngIf="errorMessage" class="error" role="alert">{{ errorMessage }}</div>

        <div class="actions">
          <button (click)="confirmar()" class="btn-cobrar"
                  [disabled]="!canConfirm()">
            {{ loading ? 'Procesando...' : 'Confirmar Cobro' }}
          </button>
          <button (click)="cancelar.emit()" class="btn-cancelar">Cancelar</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cobro-container { padding: 1rem; max-width: 480px; }
    .resumen { background: #f9f9f9; border-radius: 8px; padding: 1rem; margin-bottom: 1rem; }
    .item-row { display: flex; justify-content: space-between; padding: .25rem 0; font-size: .9rem; flex-wrap: wrap; gap: .25rem; }
    .mod { color: #888; font-style: italic; font-size: .8rem; flex-basis: 100%; }
    .precio { font-weight: 500; }
    .total-row { display: flex; justify-content: space-between; padding: .25rem 0; }
    .total-final { font-size: 1.1rem; margin-top: .5rem; }
    .pago-form { display: flex; flex-direction: column; gap: .75rem; }
    .efectivo-section { display: flex; flex-direction: column; gap: .5rem; }
    .cambio { color: #27ae60; font-weight: 600; }
    .actions { display: flex; gap: .5rem; }
    .btn-cobrar { flex: 1; padding: .75rem; background: #27ae60; color: #fff; border: none; border-radius: 4px; font-size: 1rem; cursor: pointer; }
    .btn-cobrar:disabled { opacity: .6; cursor: not-allowed; }
    .btn-cancelar { padding: .75rem 1rem; background: #eee; border: none; border-radius: 4px; cursor: pointer; }
    .error { color: #e74c3c; font-size: .875rem; }
    select, input { padding: .5rem; border: 1px solid #ccc; border-radius: 4px; font-size: 1rem; }
    hr { border: none; border-top: 1px solid #ddd; margin: .5rem 0; }
  `]
})
export class CobroComponent {
  @Input() cuentaId!: string;
  @Input() mesaNombre = '';
  @Input() items: any[] = [];
  @Input() total = 0;
  @Output() cobrado = new EventEmitter<any>();
  @Output() cancelar = new EventEmitter<void>();

  metodoPago = 'EFECTIVO';
  montoRecibido = 0;
  loading = false;
  errorMessage = '';

  get subtotal(): number {
    return this.total / 1.16;
  }

  get impuestos(): number {
    return this.total - this.subtotal;
  }

  canConfirm(): boolean {
    if (this.loading) return false;
    if (this.metodoPago === 'EFECTIVO') {
      return this.montoRecibido >= this.total;
    }
    return true;
  }

  confirmar(): void {
    this.loading = true;
    this.errorMessage = '';

    const body: any = { metodoPago: this.metodoPago };
    if (this.metodoPago === 'EFECTIVO') {
      body.montoRecibido = this.montoRecibido;
    }

    const http = (window as any).__httpClient;
    // Use injected HttpClient via constructor in real usage
    fetch(`${environment.apiUrl}/api/v1/cuentas/${this.cuentaId}/cobro`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${sessionStorage.getItem('pos_token')}`
      },
      body: JSON.stringify(body)
    })
    .then(r => r.json())
    .then(comprobante => {
      this.loading = false;
      this.cobrado.emit(comprobante);
    })
    .catch(() => {
      this.loading = false;
      this.errorMessage = 'Error al procesar el cobro. Intente nuevamente.';
    });
  }
}
