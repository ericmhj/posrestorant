package com.restaurant.pos.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.util.Map;

@ServerEndpoint("/ws")
@ApplicationScoped
public class PosWebSocketEndpoint {

    private static final Logger LOG = Logger.getLogger(PosWebSocketEndpoint.class);

    @Inject
    WebSocketBroadcastService broadcastService;

    @OnOpen
    public void onOpen(Session session) {
        broadcastService.register(session);
        // Send current state to newly connected client
        broadcastService.sendSyncState(session, Map.of(
                "message", "Connected to POS WebSocket",
                "sessionId", session.getId()
        ));
    }

    @OnClose
    public void onClose(Session session) {
        broadcastService.unregister(session);
        LOG.infof("WebSocket closed: sessionId=%s", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.errorf("WebSocket error: sessionId=%s error=%s",
                session.getId(), throwable.getMessage());
        broadcastService.unregister(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Client can send subscription messages (e.g. subscribe to a cuentaId)
        LOG.debugf("WebSocket message from sessionId=%s: %s", session.getId(), message);
    }
}
