package com.restaurant.pos.auth;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JAX-RS name-binding annotation that marks a resource class or method as requiring
 * a valid session token.
 *
 * <p>Apply this annotation to any resource method or class that should be protected
 * by {@link AuthenticationFilter}.
 *
 * <pre>
 *   &#64;GET
 *   &#64;Authenticated
 *   public Response protectedEndpoint() { ... }
 * </pre>
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Authenticated {
}
