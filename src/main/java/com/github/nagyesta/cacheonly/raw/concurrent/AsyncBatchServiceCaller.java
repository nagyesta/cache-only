package com.github.nagyesta.cacheonly.raw.concurrent;

import com.github.nagyesta.cacheonly.core.conurrent.ConcurrentOperationSupport;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;

/**
 * Defines how the underlying batch service will behave when we interact with it.
 *
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 */
public interface AsyncBatchServiceCaller<BR, BS> extends BatchServiceCaller<BR, BS>, ConcurrentOperationSupport {

}
