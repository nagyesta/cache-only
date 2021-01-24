package com.github.nagyesta.cacheonly.core.exception;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is used when a cache miss is observed and the configuration
 * does not allow us to continue and try and fetch more items from the cache.
 */
public class CacheMissException extends RuntimeException {

    /**
     * See {@link Exception#Exception(String)}.
     *
     * @param message The message detailing the cache miss.
     */
    public CacheMissException(final @NotNull String message) {
        super(message);
    }

    /**
     * See {@link Exception#Exception(String, Throwable)}.
     *
     * @param cause The cause of the failure.
     */
    public CacheMissException(final Throwable cause) {
        super("Failed to fetch item from cache.", cause);
    }
}
