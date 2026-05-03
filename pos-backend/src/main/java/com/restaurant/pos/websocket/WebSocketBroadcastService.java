package com.restaurant.pos.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebSocketBroadcastService {

    private static final Logger LOG = Logger.getLogger(WebSocketBroadcastService.class);

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public WebSocketBroadcastService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public void register(Session session) {
        sessions.put(session.getId(), session);
        LOG.infof("WebSocket client connected: sessionId=%s total=%d",
                session.getId(), sessions.size());
    }

    public void unregister(Session session) {
        sessions.remove(session.getId());
        LOG.infof("WebSocket client disconnected: sessionId=%s total=%d",
                session.getId(), sessions.size());
    }

    /**
     * Broadcasts an event to all connected clients asynchronously.
     */
    public void broadcast(WebSocketEvent event) {
        String json = toJson(event);
        if (json == null) return;

        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (!session.isOpen()) {
                LOG.infof("Removing closed session: %s", entry.getKey());
                return true;
            }
            try {
                session.getAsyncRemote().sendText(json);
            } catch (Exception e) {
                LOG.warnf("Failed to send to session %s: %s", entry.getKey(), e.getMessage());
            }
            return false;
        });
    }

    /**
     * Sends a SYNC_STATE event to a specific session on reconnect.
     */
    public void sendSyncState(Session session, Object statePayload) {
        WebSocketEvent event = new WebSocketEvent(WebSocketEventTipo.SYNC_STATE, statePayload);
        String json = toJson(event);
        if (json == null || !session.isOpen()) return;
        try {
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            LOG.warnf("Failed to send SYNC_STATE to session %s: %s",
                    session.getId(), e.getMessage());
        }
    }

    /**
     * Broadcasts a PEDIDO_CREADO event for KDS (cocina items).
     */
    public void broadcastToKDS(Object payload) {
        broadcast(new WebSocketEvent(WebSocketEventTipo.PEDIDO_CREADO, payload));
    }

    /**
     * Broadcasts a PEDIDO_CREADO event for BDS (barra items).
     */
    public void broadcastToBDS(Object payload) {
        broadcast(new WebSocketEvent(WebSocketEventTipo.PEDIDO_CREADO, payload));
    }

    /**
     * Broadcasts an ITEM_ESTADO_CAMBIADO event.
     */
    public void broadcastItemEstadoCambiado(Object payload) {
        broadcast(new WebSocketEvent(WebSocketEventTipo.ITEM_ESTADO_CAMBIADO, payload));
    }

    /**
     * Broadcasts a PEDIDO_COMPLETO event.
     */
    public void broadcastPedidoCompleto(Object payload) {
        broadcast(new WebSocketEvent(WebSocketEventTipo.PEDIDO_COMPLETO, payload));
    }

    /**
     * Broadcasts a MESA_ESTADO_CAMBIADO event.
     */
    public void broadcastMesaEstadoCambiado(Object payload) {
        broadcast(new WebSocketEvent(WebSocketEventTipo.MESA_ESTADO_CAMBIADO, payload));
    }

    /**
     * Broadcasts a STOCK_ALERTA event.
     */
    public void broadcastStockAlerta(Object payload) {
        broadcast(new WebSocketEvent(WebSocketEventTipo.STOCK_ALERTA, payload));
    }

    public int getConnectedCount() {
        return sessions.size();
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            LOG.errorf("Failed to serialize WebSocket event: %s", e.getMessage());
            return null;
        }
    }
}
