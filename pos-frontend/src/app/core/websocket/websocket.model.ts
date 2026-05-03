export type WebSocketEventTipo =
  | 'PEDIDO_CREADO'
  | 'ITEM_ESTADO_CAMBIADO'
  | 'PEDIDO_COMPLETO'
  | 'MESA_ESTADO_CAMBIADO'
  | 'STOCK_ALERTA'
  | 'SYNC_STATE'
  | 'BACKEND_UNAVAILABLE';

export interface WebSocketEvent {
  tipo: WebSocketEventTipo;
  payload: any;
  timestamp: string;
}

export type ConnectionStatus = 'connected' | 'reconnecting' | 'error' | 'disconnected';
