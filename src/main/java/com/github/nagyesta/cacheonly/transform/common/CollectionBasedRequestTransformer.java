package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Abstract transformer intended to be used in cases when the batch request is a simple
 * {@link Collection} of the partial requests.
 *
 * @param <C> The {@link Collection} type used for the batch.
 * @param <P> The type of the partial request.
 * @param <I> The key type we want to use in the map.
 */
public class CollectionBasedRequestTransformer<C extends Collection<P>, P, I>
        extends AbstractCollectionBasedTransformer<C, P, I>
        implements BatchRequestTransformer<C, P, I> {

    /**
     * Creates a new instance and defines how the {@link Collection} should be collected from a {@link java.util.stream.Stream}.
     *
     * @param collectionCollector The {@link Collector} we want to use to get a batch from a stream of elements.
     * @param idFunction          The transformation that can determine the Id of a given partial request.
     */
    public CollectionBasedRequestTransformer(final @NotNull Collector<P, ?, C> collectionCollector,
                                             final @NotNull Function<P, I> idFunction) {
        super(collectionCollector, idFunction);
    }

    @NotNull
    @Override
    public Map<I, P> splitToPartialRequest(final @NotNull C batchRequest) {
        return splitToMap(batchRequest);
    }

    @Nullable
    @Override
    public C mergeToBatchRequest(final @NotNull Map<I, P> requestMap) {
        return mergeToBatch(requestMap);
    }

}
