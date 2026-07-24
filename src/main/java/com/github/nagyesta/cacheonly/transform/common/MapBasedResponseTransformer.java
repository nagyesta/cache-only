package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Abstract transformer intended to be used in cases when the batch response is a
 * simple {@link Map} of the partial responses using the ID as a key.
 *
 * @param <C> The {@link Map} type used for the batch.
 * @param <P> The type of the partial response payload.
 * @param <I> The key type we want to use in the map.
 */
public class MapBasedResponseTransformer<C extends Map<I, P>, P, I>
        extends AbstractMapBasedTransformer<C, P, I>
        implements BatchResponseTransformer<C, P, I> {
    /**
     * Creates a new instance and provides a supplier for creating the Map of the batch response.
     *
     * @param instanceSupplier Supplies a {@link Map} instance for the merge operation.
     */
    public MapBasedResponseTransformer(final Supplier<C> instanceSupplier) {
        super(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (t, u) -> t, instanceSupplier));
    }

    /**
     * Creates a new instance and defines all parameters we can use for customization.
     *
     * @param mergeMapCollector     The collector we want to use when we merge partial responses to a batch.
     * @param splitKeyTransformer   The Function we need to use when we split the batch for transforming
     *                              an Entry to a key in the partial response entry.
     * @param splitValueTransformer The Function we need to use when we split the batch for transforming
     *                              an Entry to a value in the partial response entry.
     * @param nullIfEmpty           True is we need to return null in case we are merging an empty map.
     */
    public MapBasedResponseTransformer(
            final Collector<Map.Entry<I, P>, ?, C> mergeMapCollector,
            final Function<Map.Entry<I, P>, I> splitKeyTransformer,
            final Function<Map.Entry<I, P>, P> splitValueTransformer,
            final boolean nullIfEmpty) {
        super(mergeMapCollector, splitKeyTransformer, splitValueTransformer, nullIfEmpty);
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
