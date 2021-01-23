package com.github.nagyesta.cacheonly.transform.concurrent;

import com.github.nagyesta.cacheonly.core.conurrent.ConcurrentOperationSupport;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;

/**
 * Adds asynchronous call support to {@link PartialCacheSupport}.
 *
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the request Id.
 */
public interface AsyncPartialCacheSupport<PR, PS, C, I> extends PartialCacheSupport<PR, PS, C, I>, ConcurrentOperationSupport {

}
