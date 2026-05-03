package com.restaurant.pos.common;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

/**
 * Reusable pagination parameters injected via {@code @BeanParam} in resource methods.
 *
 * <pre>
 *   public Response list(@BeanParam PaginationParams pagination) { ... }
 * </pre>
 */
public class PaginationParams {

    @QueryParam("page")
    @DefaultValue("0")
    public int page;

    @QueryParam("size")
    @DefaultValue("20")
    public int size;

    /**
     * Returns the effective page size, capped at 100 to prevent oversized queries.
     */
    public int getSize() {
        return Math.min(size, 100);
    }

    public int getPage() {
        return Math.max(page, 0);
    }
}
