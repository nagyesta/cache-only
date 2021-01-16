package com.github.nagyesta.cacheonly.core.exception;

/**
 * This exception is used when a cache miss is observed and the configuration
 * does not allow us to continue and try and fetch more items from the cache.
 */
public class CacheMissException extends Exception {

    /**
     * See {@link Exception#Exception(String)}.
     *
     * @param message The message detailing the cache miss.
     */
    public CacheMissException(final String message) {
        super(message);
    }
}
