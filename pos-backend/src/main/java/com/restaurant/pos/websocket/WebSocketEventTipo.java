package com.restaurant.pos.websocket;

public enum WebSocketEventTipo {
    PEDIDO_CREADO,
    ITEM_ESTADO_CAMBIADO,
    PEDIDO_COMPLETO,
    MESA_ESTADO_CAMBIADO,
    STOCK_ALERTA,
    SYNC_STATE,
    BACKEND_UNAVAILABLE
}
