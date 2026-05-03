package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import com.restaurant.pos.usuario.Usuario;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * JAX-RS filter that enforces role-based access control (RBAC).
 *
 * <p>Reads the {@link RequiresRole} annotation from the matched resource method or class,
 * then checks whether the authenticated user's role is in the allowed list.
 * Aborts with HTTP 403 if the role is not permitted.
 *
 * <p>This filter runs after {@link AuthenticationFilter} (AUTHORIZATION priority > AUTHENTICATION).
 */
@Provider
@RequiresRole({})
@Priority(Priorities.AUTHORIZATION)
public class RoleAuthorizationFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RoleAuthorizationFilter.class);

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        RequiresRole annotation = getRequiresRoleAnnotation();
        if (annotation == null || annotation.value().length == 0) {
            // No role restriction — allow through (authentication already verified)
            return;
        }

        Usuario currentUser = (Usuario) requestContext.getProperty("currentUser");
        if (currentUser == null) {
            // Should not happen if AuthenticationFilter ran first, but guard anyway
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        Rol[] allowedRoles = annotation.value();
        boolean hasRole = Arrays.asList(allowedRoles).contains(currentUser.rol);

        if (!hasRole) {
            LOG.warnf("Access denied for user %s with role %s — required: %s",
                    currentUser.id, currentUser.rol, Arrays.toString(allowedRoles));
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private RequiresRole getRequiresRoleAnnotation() {
        Method method = resourceInfo.getResourceMethod();
        if (method != null) {
            RequiresRole methodAnnotation = method.getAnnotation(RequiresRole.class);
            if (methodAnnotation != null) {
                return methodAnnotation;
            }
        }
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (resourceClass != null) {
            return resourceClass.getAnnotation(RequiresRole.class);
        }
        return null;
    }
}
