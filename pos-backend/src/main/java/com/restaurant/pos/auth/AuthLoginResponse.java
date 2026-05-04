package com.restaurant.pos.auth;

import java.time.Instant;
import java.util.UUID;

public record AuthLoginResponse(String token, UsuarioInfo usuario, Instant expiresAt) {

    public record UsuarioInfo(UUID id, String nombre, String apellido, String rol) {}
}
