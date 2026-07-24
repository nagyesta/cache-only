package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Abstract transformer intended to be used in cases when the batch request is a
 * simple {@link Map} of the partial requests using the ID as a key.
 *
 * @param <C> The {@link Map} type used for the batch.
 * @param <P> The type of the partial request payload.
 * @param <I> The key type we want to use in the map.
 */
public class MapBasedRequestTransformer<C extends Map<I, P>, P, I>
        extends AbstractMapBasedTransformer<C, P, I>
        implements BatchRequestTransformer<C, P, I> {

    /**
     * Creates a new instance and provides a supplier for creating the Map of the batch request.
     *
     * @param instanceSupplier Supplies a {@link Map} instance for the merge operation.
     */
    public MapBasedRequestTransformer(final Supplier<C> instanceSupplier) {
        super(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (t, u) -> t, instanceSupplier));
    }

    /**
     * Creates a new instance and defines all parameters we can use for customization.
     *
     * @param mergeMapCollector     The collector we want to use when we merge partial request to a batch.
     * @param splitKeyTransformer   The Function we need to use when we split the batch for transforming
     *                              an Entry to a key in the partial request entry.
     * @param splitValueTransformer The Function we need to use when we split the batch for transforming
     *                              an Entry to a value in the partial request entry.
     */
    public MapBasedRequestTransformer(
            final Collector<Map.Entry<I, P>, ?, C> mergeMapCollector,
            final Function<Map.Entry<I, P>, I> splitKeyTransformer,
            final Function<Map.Entry<I, P>, P> splitValueTransformer) {
        super(mergeMapCollector, splitKeyTransformer, splitValueTransformer, false);
    }

    @Override
    public Map<I, P> splitToPartialRequest(final C batchRequest) {
        return splitToMap(batchRequest);
    }

    @Override
    public @Nullable C mergeToBatchRequest(final Map<I, P> requestMap) {
        return mergeToBatch(requestMap);
    }


}
