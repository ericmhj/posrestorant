import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebSocketService } from '../../core/websocket/websocket.service';
import { ProductPlaceholderComponent } from '../../shared/components/product-placeholder';

export interface MesaDTO {
  id: string;
  nombre: string;
  estado: 'LIBRE' | 'OCUPADA' | 'RESERVADA';
  cuentaId?: string;
  totalAcumulado?: number;
  tiempoAbierta?: number;
}

@Component({
  selector: 'app-pos',
  standalone: true,
  imports: [CommonModule, ProductPlaceholderComponent],
  template: `
    <div class="pos-container">
      <h2>Mapa de Mesas</h2>
      <div class="mesa-grid">
        <div *ngFor="let mesa of mesas"
             class="mesa-card"
             [class.libre]="mesa.estado === 'LIBRE'"
             [class.ocupada]="mesa.estado === 'OCUPADA'"
             [class.reservada]="mesa.estado === 'RESERVADA'"
             (click)="onMesaClick(mesa)"
             [attr.aria-label]="mesa.nombre + ' - ' + mesa.estado"
             role="button" tabindex="0">
          <div class="mesa-nombre">{{ mesa.nombre }}</div>
          <div class="mesa-estado">{{ mesa.estado }}</div>
          <div *ngIf="mesa.estado === 'OCUPADA'" class="mesa-info">
            <span>{{ formatTime(mesa.tiempoAbierta) }}</span>
            <span>${{ mesa.totalAcumulado | number:'1.2-2' }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .pos-container { padding: 1rem; }
    .mesa-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; }
    .mesa-card { padding: 1rem; border-radius: 8px; cursor: pointer; text-align: center; border: 2px solid transparent; transition: transform .15s; }
    .mesa-card:hover { transform: scale(1.03); }
    .libre { background: #27ae60; color: #fff; }
    .ocupada { background: #e74c3c; color: #fff; }
    .reservada { background: #f39c12; color: #fff; }
    .mesa-nombre { font-weight: 700; font-size: 1.1rem; }
    .mesa-estado { font-size: .75rem; opacity: .85; margin-top: .25rem; }
    .mesa-info { font-size: .8rem; margin-top: .5rem; display: flex; flex-direction: column; gap: .2rem; }
  `]
})
export class PosComponent implements OnInit, OnDestroy {
  mesas: MesaDTO[] = [];
  private sub?: Subscription;

  constructor(private http: HttpClient, private ws: WebSocketService) {}

  ngOnInit(): void {
    this.loadMesas();
    this.ws.connect();
    this.sub = this.ws.events$.subscribe(event => {
      if (event.tipo === 'MESA_ESTADO_CAMBIADO') this.loadMesas();
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  loadMesas(): void {
    this.http.get<MesaDTO[]>(`${environment.apiUrl}/api/v1/mesas`)
      .subscribe(mesas => this.mesas = mesas);
  }

  onMesaClick(mesa: MesaDTO): void {
    if (mesa.estado === 'LIBRE') {
      this.http.post(`${environment.apiUrl}/api/v1/mesas/${mesa.id}/abrir`, {})
        .subscribe(() => this.loadMesas());
    }
  }

  formatTime(seconds?: number): string {
    if (!seconds) return '0:00';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
