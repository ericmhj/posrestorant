package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Usuario;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.UUID;

/**
 * JAX-RS filter that validates the Bearer token on every request.
 * Public paths (login) are excluded via annotation or path check.
 */
@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Inject
    SessionStore sessionStore;

    @Inject
    CurrentUser currentUser;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Allow public endpoints without authentication
        if (path.startsWith("/api/v1/auth/login") ||
            path.startsWith("/q/") ||
            path.startsWith("/uploads/")) {
            return;
        }

        String authHeader = requestContext.getHeaderString(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            abort(requestContext, "Token de autenticación requerido");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (!sessionStore.isValid(token)) {
            abort(requestContext, "Token inválido o sesión expirada");
            return;
        }

        UUID userId = sessionStore.getUserId(token);
        Usuario usuario = Usuario.findById(userId).orElse(null);

        if (usuario == null || !Boolean.TRUE.equals(usuario.activo)) {
            sessionStore.invalidate(token);
            abort(requestContext, "Usuario no encontrado o inactivo");
            return;
        }

        currentUser.setUsuario(usuario);
        // Store token in context for logout
        requestContext.setProperty("authToken", token);
    }

    private void abort(ContainerRequestContext ctx, String message) {
        ctx.abortWith(Response.status(401)
                .entity("{\"message\":\"" + message + "\"}")
                .type("application/json")
                .build());
    }
}
