package com.github.nagyesta.cacheonly.raw.exception;

import org.springframework.lang.NonNull;

/**
 * Exception class used for indicating fetch failure while we try to get up-to-date
 * information from the batch service we try to wrap with a cache.
 */
public class BatchServiceException extends Exception {

    /**
     * See {@link Exception#Exception(String)}.
     *
     * @param message The message detailing the failure.
     */
    public BatchServiceException(@NonNull final String message) {
        super(message);
    }

    /**
     * See {@link Exception#Exception(String, Throwable)}.
     *
     * @param message The message detailing the failure.
     * @param cause   The cause of the failure.
     */
    public BatchServiceException(@NonNull final String message,
                                 @NonNull final Throwable cause) {
        super(message, cause);
    }
}
