import { Injectable, OnDestroy } from '@angular/core';
import { Subject, BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebSocketEvent, ConnectionStatus } from './websocket.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private readonly MAX_ATTEMPTS = 5;
  private readonly BACKOFF_DELAYS = [1000, 2000, 4000, 8000, 16000];
  private reconnectTimer: any;
  private destroyed = false;

  private eventsSubject = new Subject<WebSocketEvent>();
  private statusSubject = new BehaviorSubject<ConnectionStatus>('disconnected');

  events$: Observable<WebSocketEvent> = this.eventsSubject.asObservable();
  connectionStatus$: Observable<ConnectionStatus> = this.statusSubject.asObservable();

  connect(): void {
    if (this.socket?.readyState === WebSocket.OPEN) return;
    this.createConnection();
  }

  disconnect(): void {
    this.destroyed = true;
    clearTimeout(this.reconnectTimer);
    this.socket?.close();
    this.statusSubject.next('disconnected');
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private createConnection(): void {
    try {
      this.socket = new WebSocket(environment.wsUrl);

      this.socket.onopen = () => {
        this.reconnectAttempts = 0;
        this.statusSubject.next('connected');
      };

      this.socket.onmessage = (event) => {
        try {
          const wsEvent: WebSocketEvent = JSON.parse(event.data);
          this.eventsSubject.next(wsEvent);
        } catch {
          // ignore malformed messages
        }
      };

      this.socket.onclose = () => {
        if (!this.destroyed) this.scheduleReconnect();
      };

      this.socket.onerror = () => {
        if (!this.destroyed) this.scheduleReconnect();
      };
    } catch {
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect(): void {
    if (this.destroyed) return;

    if (this.reconnectAttempts >= this.MAX_ATTEMPTS) {
      this.statusSubject.next('error');
      return;
    }

    this.statusSubject.next('reconnecting');
    const delay = this.BACKOFF_DELAYS[
      Math.min(this.reconnectAttempts, this.BACKOFF_DELAYS.length - 1)
    ];
    this.reconnectAttempts++;

    this.reconnectTimer = setTimeout(() => {
      if (!this.destroyed) this.createConnection();
    }, delay);
  }
}
