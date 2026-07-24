package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Abstract transformer intended to be used in cases when the batch response is a simple
 * {@link Collection} of the partial responses.
 *
 * @param <C> The {@link Collection} type used for the batch.
 * @param <P> The type of the partial response.
 * @param <I> The key type we want to use in the map.
 */
public class CollectionBasedResponseTransformer<C extends Collection<P>, P, I>
        extends AbstractCollectionBasedTransformer<C, P, I>
        implements BatchResponseTransformer<C, P, I> {

    /**
     * Creates a new instance and defines how the {@link Collection} should be collected from a {@link java.util.stream.Stream}.
     *
     * @param collectionCollector The {@link Collector} we want to use to get a batch from a stream of elements.
     * @param idFunction          The transformation that can determine the ID of a given partial response.
     */
    public CollectionBasedResponseTransformer(
            final Collector<P, ?, C> collectionCollector,
            final Function<P, I> idFunction) {
        super(collectionCollector, idFunction);
    }

    /**
     * Creates a new instance and defines how the {@link Collection} should be collected from a {@link java.util.stream.Stream}.
     *
     * @param collectionCollector The {@link Collector} we want to use to get a batch from a stream of elements.
     * @param idFunction          The transformation that can determine the ID of a given partial response.
     * @param nullIfEmpty         Flag telling the implementation whether we want to use null in case of an empty {@link Collection}.
     */
    public CollectionBasedResponseTransformer(
            final Collector<P, ?, C> collectionCollector,
            final Function<P, I> idFunction,
            final boolean nullIfEmpty) {
        super(collectionCollector, idFunction, nullIfEmpty);
    }

    @Override
    public Map<I, P> splitToPartialResponse(final C batchResponse) {
        return splitToMap(batchResponse);
    }

    @Override
    public @Nullable C mergeToBatchResponse(final Map<I, P> entityMap) {
        return mergeToBatch(entityMap);
    }


}
