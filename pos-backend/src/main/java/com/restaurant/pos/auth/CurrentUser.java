package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import com.restaurant.pos.usuario.Usuario;
import jakarta.enterprise.context.RequestScoped;

import java.util.UUID;

/**
 * Request-scoped CDI bean holding the authenticated user for the current request.
 * Populated by {@link AuthenticationFilter}.
 */
@RequestScoped
public class CurrentUser {

    private Usuario usuario;

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public UUID getId() {
        return usuario != null ? usuario.id : null;
    }

    public Rol getRol() {
        return usuario != null ? usuario.rol : null;
    }

    public boolean hasRole(Rol rol) {
        return usuario != null && rol.equals(usuario.rol);
    }

    public boolean isAuthenticated() {
        return usuario != null;
    }
}
