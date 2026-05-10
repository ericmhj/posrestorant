import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoadingOverlayComponent } from '../../shared/components/loading-overlay/loading-overlay.component';
import { WebSocketService } from '../../core/websocket/websocket.service';

@Component({
  selector: 'app-cuenta',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingOverlayComponent],
  template: `
    <app-loading-overlay [visible]="loading"></app-loading-overlay>

    <!-- Toast notificación -->
    <div *ngIf="toast" class="toast" [class.toast-listo]="toast.tipo === 'listo'">
      🔔 {{ toast.mensaje }}
    </div>

    <div class="cuenta-container">
      <div class="cuenta-header">
        <button (click)="volver()" class="btn-back">← Mesas</button>
        <h2>{{ mesaNombre }} — Cuenta</h2>
        <span class="estado-badge" [class.abierta]="cuenta?.estado === 'ABIERTA'">
          {{ cuenta?.estado }}
        </span>
      </div>

      <div class="cuenta-layout">
        <!-- Panel izquierdo: productos -->
        <div class="productos-panel" *ngIf="cuenta?.estado === 'ABIERTA'">
          <h3>Agregar productos</h3>

          <div class="categoria-tabs">
            <button *ngFor="let cat of categorias"
                    (click)="categoriaSeleccionada = cat.id"
                    [class.active]="categoriaSeleccionada === cat.id"
                    class="tab-btn">
              {{ cat.nombre }}
            </button>
          </div>

          <div class="productos-grid">
            <div *ngFor="let p of productosFiltrados()"
                 class="producto-btn"
                 (click)="agregarAlCarrito(p)">
              <span class="prod-nombre">{{ p.nombre }}</span>
              <span class="prod-precio">$ {{ p.precio | number:'1.2-2' }}</span>
            </div>
          </div>

          <!-- Carrito -->
          <div *ngIf="carrito.length > 0" class="carrito">
            <h4>Pedido actual</h4>
            <div *ngFor="let item of carrito; let i = index" class="carrito-item">
              <span>{{ item.cantidad }}x {{ item.nombre }}</span>
              <span>$ {{ (item.precio * item.cantidad) | number:'1.2-2' }}</span>
              <div class="carrito-controls">
                <button (click)="decrementar(i)">−</button>
                <span>{{ item.cantidad }}</span>
                <button (click)="incrementar(i)">+</button>
                <button (click)="quitarDelCarrito(i)" class="btn-remove">🗑</button>
              </div>
            </div>
            <button (click)="enviarPedido()" class="btn-enviar">
              Enviar pedido ({{ carrito.length }} items)
            </button>
          </div>
        </div>

        <!-- Panel derecho: resumen cuenta -->
        <div class="resumen-panel">
          <h3>Resumen</h3>

          <div *ngIf="pedidos.length === 0" class="empty-msg">Sin pedidos aún</div>

          <div *ngFor="let pedido of pedidos" class="pedido-ronda">
            <div class="ronda-header">Ronda {{ pedido.numeroRonda }}</div>
            <div *ngFor="let item of pedido.items" class="item-row">
              <span>{{ item.cantidad }}x {{ item.productoNombre }}</span>
              <span class="item-estado" [ngClass]="'estado-' + item.estado?.toLowerCase()">
                {{ estadoLabel(item.estado) }}
              </span>
              <span>$ {{ (item.precioUnitario * item.cantidad) | number:'1.2-2' }}</span>
              <button *ngIf="item.estado === 'PENDIENTE' && cuenta?.estado === 'ABIERTA'"
                      (click)="eliminarItem(pedido.id, item.id)"
                      class="btn-remove-item">✕</button>
              <button *ngIf="item.estado === 'LISTO'"
                      (click)="entregarItem(item.id)"
                      class="btn-entregar">📦 Entregar</button>
            </div>
          </div>

          <div class="totales" *ngIf="cuenta">
            <div class="total-row"><span>Subtotal:</span><span>$ {{ subtotal | number:'1.2-2' }}</span></div>
            <div class="total-row"><span>IVA (16%):</span><span>$ {{ impuestos | number:'1.2-2' }}</span></div>
            <div class="total-row total-final"><strong>Total:</strong><strong>$ {{ cuenta.total | number:'1.2-2' }}</strong></div>
          </div>

          <!-- Cobro -->
          <div *ngIf="cuenta?.estado === 'ABIERTA' && pedidos.length > 0" class="cobro-section">
            <h4>Cobrar</h4>
            <select [(ngModel)]="metodoPago">
              <option value="EFECTIVO">Efectivo</option>
              <option value="TARJETA_CREDITO">Tarjeta de Crédito</option>
              <option value="TARJETA_DEBITO">Tarjeta de Débito</option>
            </select>
            <div *ngIf="metodoPago === 'EFECTIVO'" class="efectivo">
              <label>Monto recibido:
                <input type="number" [(ngModel)]="montoRecibido" [min]="cuenta.total" step="0.01" />
              </label>
              <div *ngIf="montoRecibido >= cuenta.total" class="cambio">
                Cambio: $ {{ (montoRecibido - cuenta.total) | number:'1.2-2' }}
              </div>
            </div>
            <div *ngIf="cobroError" class="error">{{ cobroError }}</div>
            <button (click)="cobrar()" class="btn-cobrar" [disabled]="!puedeCobrarse()">
              Confirmar Cobro
            </button>
          </div>

          <!-- Comprobante -->
          <div *ngIf="comprobante" class="comprobante">
            <h4>✅ Cobro exitoso</h4>
            <p>Total: $ {{ comprobante.total | number:'1.2-2' }}</p>
            <p *ngIf="comprobante.cambio">Cambio: $ {{ comprobante.cambio | number:'1.2-2' }}</p>
            <button (click)="volver()" class="btn-enviar">Volver a mesas</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cuenta-container { padding: 1rem; }
    .cuenta-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem; }
    .cuenta-header h2 { margin: 0; flex: 1; }
    .btn-back { padding: .4rem .8rem; background: #eee; border: none; border-radius: 4px; cursor: pointer; }
    .estado-badge { padding: .3rem .7rem; border-radius: 12px; font-size: .8rem; background: #eee; }
    .estado-badge.abierta { background: #d5f5e3; color: #27ae60; }
    .cuenta-layout { display: flex; gap: 1.5rem; align-items: flex-start; }
    .productos-panel { flex: 1; }
    .resumen-panel { width: 360px; min-width: 300px; background: #f9f9f9; border-radius: 8px; padding: 1rem; position: sticky; top: 1rem; }
    .categoria-tabs { display: flex; flex-wrap: wrap; gap: .4rem; margin-bottom: .75rem; }
    .tab-btn { padding: .3rem .7rem; border: 1px solid #ccc; border-radius: 12px; background: #fff; cursor: pointer; font-size: .85rem; }
    .tab-btn.active { background: #2980b9; color: #fff; border-color: #2980b9; }
    .productos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: .5rem; margin-bottom: 1rem; }
    .producto-btn { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: .75rem; cursor: pointer; text-align: center; transition: background .15s; }
    .producto-btn:hover { background: #ebf5fb; }
    .prod-nombre { display: block; font-weight: 600; font-size: .9rem; }
    .prod-precio { display: block; color: #2980b9; font-size: .85rem; margin-top: .2rem; }
    .carrito { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: .75rem; margin-top: .75rem; }
    .carrito h4 { margin: 0 0 .5rem; }
    .carrito-item { display: flex; justify-content: space-between; align-items: center; padding: .3rem 0; font-size: .9rem; gap: .5rem; flex-wrap: wrap; }
    .carrito-controls { display: flex; align-items: center; gap: .3rem; }
    .carrito-controls button { width: 24px; height: 24px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: #fff; }
    .btn-remove { background: #e74c3c !important; color: #fff !important; border-color: #e74c3c !important; }
    .btn-enviar { width: 100%; margin-top: .75rem; padding: .6rem; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: .95rem; }
    .pedido-ronda { margin-bottom: .75rem; }
    .ronda-header { font-weight: 600; font-size: .85rem; color: #666; margin-bottom: .3rem; }
    .item-row { display: flex; align-items: center; gap: .5rem; padding: .2rem 0; font-size: .85rem; }
    .item-row span:first-child { flex: 1; }
    .item-estado { font-size: .75rem; padding: .1rem .4rem; border-radius: 8px; background: #eee; white-space: nowrap; }
    .estado-pendiente { background: #fef9e7; color: #d68910; }
    .estado-preparando { background: #eaf4fb; color: #2980b9; }
    .estado-listo { background: #d5f5e3; color: #27ae60; font-weight: 600; }
    .estado-entregado { background: #f0f0f0; color: #888; }
    .btn-remove-item { background: none; border: none; cursor: pointer; color: #e74c3c; font-size: .9rem; }
    .btn-entregar { background: #27ae60; color: #fff; border: none; border-radius: 4px; cursor: pointer; padding: .2rem .5rem; font-size: .8rem; }
    /* Toast */
    .toast { position: fixed; top: 1rem; right: 1rem; background: #2c3e50; color: #fff; padding: .75rem 1.25rem; border-radius: 8px; z-index: 10000; font-size: .95rem; box-shadow: 0 4px 12px rgba(0,0,0,.2); animation: slideIn .3s ease; }
    .toast-listo { background: #27ae60; }
    @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    .totales { border-top: 1px solid #ddd; padding-top: .5rem; margin-top: .5rem; }
    .total-row { display: flex; justify-content: space-between; padding: .2rem 0; font-size: .9rem; }
    .total-final { font-size: 1rem; margin-top: .3rem; }
    .cobro-section { border-top: 1px solid #ddd; padding-top: .75rem; margin-top: .75rem; display: flex; flex-direction: column; gap: .5rem; }
    .cobro-section h4 { margin: 0; }
    .efectivo { display: flex; flex-direction: column; gap: .4rem; }
    .cambio { color: #27ae60; font-weight: 600; font-size: .9rem; }
    .btn-cobrar { padding: .75rem; background: #27ae60; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 1rem; }
    .btn-cobrar:disabled { opacity: .6; cursor: not-allowed; }
    .error { color: #e74c3c; font-size: .85rem; }
    .comprobante { background: #d5f5e3; border-radius: 8px; padding: 1rem; text-align: center; }
    .empty-msg { color: #999; text-align: center; padding: 1rem; font-size: .9rem; }
    select, input { padding: .4rem; border: 1px solid #ccc; border-radius: 4px; width: 100%; box-sizing: border-box; }
    label { font-size: .85rem; display: flex; flex-direction: column; gap: .3rem; }
  `]
})
export class CuentaComponent implements OnInit, OnDestroy {
  cuentaId = '';
  mesaNombre = '';
  cuenta: any = null;
  pedidos: any[] = [];
  categorias: any[] = [];
  productos: any[] = [];
  categoriaSeleccionada = '';
  carrito: any[] = [];
  loading = false;
  metodoPago = 'EFECTIVO';
  montoRecibido = 0;
  cobroError = '';
  comprobante: any = null;
  toast: { mensaje: string; tipo: string } | null = null;
  private wsSub?: Subscription;
  private toastTimer?: any;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void {
    this.cuentaId = this.route.snapshot.paramMap.get('cuentaId') || '';
    this.mesaNombre = this.route.snapshot.queryParamMap.get('mesa') || 'Mesa';
    this.loadCuenta();
    this.loadCategorias();
    this.loadProductos();

    // Suscribir WebSocket para actualizaciones en tiempo real
    this.ws.connect();
    this.wsSub = this.ws.events$.subscribe(event => {
      if (event.tipo === 'ITEM_ESTADO_CAMBIADO' && event.payload?.cuentaId === this.cuentaId) {
        this.loadCuenta();
        if (event.payload?.nuevoEstado === 'LISTO') {
          this.mostrarToast(`${event.payload?.productoNombre || 'Un platillo'} está LISTO`, 'listo');
        } else if (event.payload?.nuevoEstado === 'PREPARANDO') {
          this.mostrarToast(`${event.payload?.productoNombre || 'Un platillo'} está siendo preparado`, 'info');
        }
      }
      if (event.tipo === 'PEDIDO_COMPLETO' && event.payload?.cuentaId === this.cuentaId) {
        this.mostrarToast('✅ Todos los platillos están listos', 'listo');
        this.loadCuenta();
      }
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    if (this.toastTimer) clearTimeout(this.toastTimer);
  }

  mostrarToast(mensaje: string, tipo: string): void {
    this.toast = { mensaje, tipo };
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast = null, 4000);
  }

  estadoLabel(estado: string): string {
    const labels: Record<string, string> = {
      'PENDIENTE': '⏳ Pendiente',
      'PREPARANDO': '🔥 Preparando',
      'LISTO': '✅ Listo',
      'ENTREGADO': '📦 Entregado'
    };
    return labels[estado] || estado;
  }

  loadCuenta(): void {
    this.http.get<any>(`${environment.apiUrl}/api/v1/cuentas/${this.cuentaId}`)
      .subscribe(c => {
        this.cuenta = c;
        this.pedidos = c.pedidos || [];
      });
  }

  loadCategorias(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/categorias`)
      .subscribe(c => {
        this.categorias = c.sort((a, b) => a.nombre.localeCompare(b.nombre));
        if (c.length > 0) this.categoriaSeleccionada = c[0].id;
      });
  }

  loadProductos(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/v1/productos?soloActivos=true`)
      .subscribe(p => this.productos = p.sort((a, b) => a.nombre.localeCompare(b.nombre)));
  }

  productosFiltrados(): any[] {
    return this.productos.filter(p => p.categoria?.id === this.categoriaSeleccionada);
  }

  agregarAlCarrito(p: any): void {
    const existing = this.carrito.find(i => i.productoId === p.id);
    if (existing) {
      existing.cantidad++;
    } else {
      this.carrito.push({ productoId: p.id, nombre: p.nombre, precio: p.precio, cantidad: 1 });
    }
  }

  incrementar(i: number): void { this.carrito[i].cantidad++; }

  decrementar(i: number): void {
    if (this.carrito[i].cantidad > 1) this.carrito[i].cantidad--;
    else this.quitarDelCarrito(i);
  }

  quitarDelCarrito(i: number): void { this.carrito.splice(i, 1); }

  enviarPedido(): void {
    if (this.carrito.length === 0) return;
    this.loading = true;
    const body = {
      items: this.carrito.map(i => ({ productoId: i.productoId, cantidad: i.cantidad }))
    };
    this.http.post(`${environment.apiUrl}/api/v1/cuentas/${this.cuentaId}/pedidos`, body)
      .subscribe({
        next: () => { this.carrito = []; this.loading = false; this.loadCuenta(); },
        error: () => { this.loading = false; }
      });
  }

  eliminarItem(pedidoId: string, itemId: string): void {
    this.loading = true;
    this.http.delete(`${environment.apiUrl}/api/v1/cuentas/${this.cuentaId}/pedidos/${pedidoId}/items/${itemId}`)
      .subscribe({
        next: () => { this.loading = false; this.loadCuenta(); },
        error: () => { this.loading = false; }
      });
  }

  entregarItem(itemId: string): void {
    this.loading = true;
    this.http.put(`${environment.apiUrl}/api/v1/cuentas/${this.cuentaId}/items/${itemId}/entregar`, {})
      .subscribe({
        next: () => { this.loading = false; this.loadCuenta(); },
        error: () => { this.loading = false; }
      });
  }

  get subtotal(): number { return (this.cuenta?.total || 0) / 1.16; }
  get impuestos(): number { return (this.cuenta?.total || 0) - this.subtotal; }

  puedeCobrarse(): boolean {
    if (!this.cuenta || this.pedidos.length === 0) return false;
    if (this.metodoPago === 'EFECTIVO') return this.montoRecibido >= this.cuenta.total;
    return true;
  }

  cobrar(): void {
    this.loading = true;
    this.cobroError = '';
    const body: any = { metodoPago: this.metodoPago };
    if (this.metodoPago === 'EFECTIVO') body.montoRecibido = this.montoRecibido;
    this.http.post(`${environment.apiUrl}/api/v1/cuentas/${this.cuentaId}/cobro`, body)
      .subscribe({
        next: (comp) => { this.loading = false; this.comprobante = comp; this.loadCuenta(); },
        error: (e) => { this.loading = false; this.cobroError = e?.error?.message || 'Error al cobrar'; }
      });
  }

  volver(): void { this.router.navigate(['/pos']); }
}
