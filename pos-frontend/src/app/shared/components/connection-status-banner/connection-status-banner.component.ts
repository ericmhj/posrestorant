import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebSocketService } from '../../../core/websocket/websocket.service';
import { ConnectionStatus } from '../../../core/websocket/websocket.model';

@Component({
  selector: 'app-connection-status-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="status === 'error' || status === 'reconnecting'"
         class="connection-banner"
         [class.error]="status === 'error'"
         [class.reconnecting]="status === 'reconnecting'"
         role="alert"
         aria-live="polite">
      <span *ngIf="status === 'reconnecting'">
        ⚠️ Reconectando al servidor...
      </span>
      <span *ngIf="status === 'error'">
        ❌ Sin conexión al servidor. Algunas funciones no están disponibles.
      </span>
    </div>
  `,
  styles: [`
    .connection-banner {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 9999;
      padding: 8px 16px;
      text-align: center;
      font-size: 0.875rem;
      font-weight: 500;
    }
    .reconnecting { background: #f39c12; color: #fff; }
    .error { background: #e74c3c; color: #fff; }
  `]
})
export class ConnectionStatusBannerComponent implements OnInit {
  status: ConnectionStatus = 'connected';

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.wsService.connectionStatus$.subscribe(s => this.status = s);
  }
}
