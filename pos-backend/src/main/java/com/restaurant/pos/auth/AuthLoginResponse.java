package com.restaurant.pos.auth;

import java.time.Instant;
import java.util.UUID;

public class AuthLoginResponse {

    public String token;
    public UsuarioInfo usuario;
    public Instant expiresAt;

    public AuthLoginResponse(String token, UsuarioInfo usuario, Instant expiresAt) {
        this.token = token;
        this.usuario = usuario;
        this.expiresAt = expiresAt;
    }

    public static class UsuarioInfo {
        public UUID id;
        public String nombre;
        public String rol;

        public UsuarioInfo(UUID id, String nombre, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.rol = rol;
        }
    }
}
