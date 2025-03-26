package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

/**
 * Transformer implementation suitable for batches which are using a wrapper class around
 * a {@link Map} of partial entities (and potentially other fields as well).
 *
 * @param <B> The type of the batch wrapper class.
 * @param <C> The type of the {@link Map} holding the values we want to partition.
 * @param <E> The type of the partial entities.
 * @param <I> The type of the ID we can use for partial entity identification.
 */
public class WrappedMapBasedRequestTransformer<B, C extends Map<I, E>, E, I>
        extends AbstractWrappedMapBasedTransformer<B, C, E, I>
        implements BatchRequestTransformer<B, B, I> {

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param instanceSupplier   The {@link Supplier} we can use for getting a new empty batch instance.
     * @param mapReadFunction    The function that can read the map from a batch.
     * @param mapWriteBiFunction The function that can write the map into a batch.
     * @param mapCollector       The collector creating a new map from the partial entities.
     */
    public WrappedMapBasedRequestTransformer(
            final @NotNull Supplier<B> instanceSupplier,
            final @NotNull Function<B, C> mapReadFunction,
            final @NotNull BiFunction<B, C, B> mapWriteBiFunction,
            final @NotNull Collector<Map.Entry<I, E>, ?, C> mapCollector) {
        super(instanceSupplier, mapReadFunction, mapWriteBiFunction, mapCollector);
    }

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param cloneFunction      The function that can clone a batch.
     * @param mapReadFunction    The function that can read the map from a batch.
     * @param mapWriteBiFunction The function that can write the map into a batch.
     * @param mapCollector       The collector creating a new map from the partial entities.
     */
    public WrappedMapBasedRequestTransformer(
            final @NotNull UnaryOperator<B> cloneFunction,
            final @NotNull Function<B, C> mapReadFunction,
            final @NotNull BiFunction<B, C, B> mapWriteBiFunction,
            final @NotNull Collector<Map.Entry<I, E>, ?, C> mapCollector) {
        super(cloneFunction, mapReadFunction, mapWriteBiFunction, mapCollector);
    }

    @NotNull
    @Override
    public Map<I, B> splitToPartialRequest(final @NotNull B batchRequest) {
        return splitToMap(batchRequest);
    }

    @Nullable
    @Override
    public B mergeToBatchRequest(final @NotNull Map<I, B> requestMap) {
        return mergeToBatch(requestMap);
    }

}
