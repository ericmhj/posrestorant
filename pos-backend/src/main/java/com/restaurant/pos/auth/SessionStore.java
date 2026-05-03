package com.restaurant.pos.auth;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session store mapping token → userId.
 * Used to invalidate sessions on logout or user deactivation.
 */
@ApplicationScoped
public class SessionStore {

    private final ConcurrentHashMap<String, UUID> sessions = new ConcurrentHashMap<>();

    public void register(String token, UUID userId) {
        sessions.put(token, userId);
    }

    public void invalidate(String token) {
        sessions.remove(token);
    }

    public void invalidateAllForUser(UUID userId) {
        sessions.entrySet().removeIf(e -> userId.equals(e.getValue()));
    }

    public boolean isValid(String token) {
        return sessions.containsKey(token);
    }

    public UUID getUserId(String token) {
        return sessions.get(token);
    }
}
