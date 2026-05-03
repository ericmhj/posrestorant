package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

/**
 * Annotation to declare required roles on JAX-RS resource methods.
 * Enforced by {@link RoleGuard}.
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {
    Rol[] value();
}
