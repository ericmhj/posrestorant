package com.restaurant.pos.websocket;

import java.time.Instant;

public class WebSocketEvent {

    public WebSocketEventTipo tipo;
    public Object payload;
    public Instant timestamp;

    public WebSocketEvent() {}

    public WebSocketEvent(WebSocketEventTipo tipo, Object payload) {
        this.tipo = tipo;
        this.payload = payload;
        this.timestamp = Instant.now();
    }
}
