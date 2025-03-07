package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Abstract transformer intended to be used in cases when the batch request is a
 * simple {@link Map} of the partial requests using the ID as key.
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
    public MapBasedRequestTransformer(final @NotNull Supplier<C> instanceSupplier) {
        super(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (T, U) -> T, instanceSupplier));
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
            final @NotNull Collector<Map.Entry<I, P>, ?, C> mergeMapCollector,
            final @NotNull Function<Map.Entry<I, P>, I> splitKeyTransformer,
            final @NotNull Function<Map.Entry<I, P>, P> splitValueTransformer) {
        super(mergeMapCollector, splitKeyTransformer, splitValueTransformer, false);
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
