package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * CDI interceptor that enforces role-based access control.
 * Aborts with HTTP 403 if the current user's role is not in the allowed list.
 */
@Interceptor
@RequiresRole({})
public class RoleGuard {

    @Inject
    CurrentUser currentUser;

    @AroundInvoke
    public Object checkRole(InvocationContext ctx) throws Exception {
        Method method = ctx.getMethod();
        RequiresRole annotation = method.getAnnotation(RequiresRole.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(RequiresRole.class);
        }

        if (annotation != null) {
            Rol[] allowedRoles = annotation.value();
            Rol userRole = currentUser.getRol();

            if (userRole == null || Arrays.stream(allowedRoles).noneMatch(r -> r == userRole)) {
                return Response.status(403)
                        .entity("{\"message\":\"No tiene permisos para realizar esta acción\"}")
                        .type("application/json")
                        .build();
            }
        }

        return ctx.proceed();
    }
}
