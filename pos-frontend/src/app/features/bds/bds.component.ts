import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subscription, interval } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebSocketService } from '../../core/websocket/websocket.service';
import { ItemPedidoDTO } from '../kds/kds.component';

@Component({
  selector: 'app-bds',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bds-container">
      <h2>Barra — BDS</h2>
      <div *ngIf="items.length === 0" class="empty">Sin pedidos pendientes</div>
      <div class="items-grid">
        <div *ngFor="let item of items"
             class="item-card"
             [class.demorado]="item.demorado">
          <div class="item-header">
            <span class="mesa">{{ item.mesaNombre }}</span>
            <span class="tiempo">{{ formatTime(item.tiempoEspera) }}</span>
          </div>
          <div class="producto">{{ item.cantidad }}x {{ item.productoNombre }}</div>
          <div *ngIf="item.modificadores" class="modificadores">{{ item.modificadores }}</div>
          <div class="estado-badge">{{ item.estado }}</div>
          <div class="actions">
            <button *ngIf="item.estado === 'PENDIENTE'"
                    (click)="updateEstado(item.id, 'PREPARANDO')">
              Preparando
            </button>
            <button *ngIf="item.estado === 'PREPARANDO'"
                    (click)="updateEstado(item.id, 'LISTO')"
                    class="listo">
              Listo ✓
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .bds-container { padding: 1rem; }
    .items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; }
    .item-card { background: #fff; border: 2px solid #ddd; border-radius: 8px; padding: 1rem; }
    .item-card.demorado { border-color: #e74c3c; background: #fdf2f2; }
    .item-header { display: flex; justify-content: space-between; margin-bottom: .5rem; }
    .mesa { font-weight: 700; }
    .tiempo { color: #666; font-size: .85rem; }
    .producto { font-size: 1.1rem; margin-bottom: .25rem; }
    .modificadores { font-size: .8rem; color: #888; font-style: italic; }
    .estado-badge { display: inline-block; padding: .2rem .5rem; border-radius: 4px; font-size: .75rem; background: #eee; margin: .5rem 0; }
    .actions button { width: 100%; padding: .5rem; border: none; border-radius: 4px; cursor: pointer; background: #8e44ad; color: #fff; }
    .actions button.listo { background: #27ae60; }
    .empty { color: #888; text-align: center; padding: 2rem; }
  `]
})
export class BdsComponent implements OnInit, OnDestroy {
  items: ItemPedidoDTO[] = [];
  private subs: Subscription[] = [];

  constructor(private http: HttpClient, private ws: WebSocketService) {}

  ngOnInit(): void {
    this.loadItems();
    this.ws.connect();
    this.subs.push(
      this.ws.events$.subscribe(e => {
        if (e.tipo === 'PEDIDO_CREADO' || e.tipo === 'ITEM_ESTADO_CAMBIADO') {
          this.loadItems();
        }
      }),
      interval(30000).subscribe(() => this.loadItems())
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  loadItems(): void {
    this.http.get<ItemPedidoDTO[]>(`${environment.apiUrl}/api/v1/bds/items`)
      .subscribe(items => this.items = items);
  }

  updateEstado(id: string, estado: string): void {
    this.http.put(`${environment.apiUrl}/api/v1/bds/items/${id}/estado`, { estado })
      .subscribe(() => this.loadItems());
  }

  formatTime(seconds?: number): string {
    if (!seconds) return '0:00';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
