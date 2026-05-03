package com.restaurant.pos.common;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T>        the type of items in the page
 * @param content    the items on this page
 * @param page       the current page index (0-based)
 * @param size       the requested page size
 * @param totalItems the total number of items across all pages
 * @param totalPages the total number of pages
 */
public record Page<T>(
        List<T> content,
        int page,
        int size,
        long totalItems,
        int totalPages
) {

    /**
     * Creates a {@link Page} from a list of items and pagination metadata.
     *
     * @param content    the items on this page
     * @param params     the pagination parameters used for the query
     * @param totalItems the total count of items (from a count query)
     * @param <T>        the item type
     * @return a populated {@link Page}
     */
    public static <T> Page<T> of(List<T> content, PaginationParams params, long totalItems) {
        int size = params.getSize();
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        return new Page<>(content, params.getPage(), size, totalItems, totalPages);
    }
}
