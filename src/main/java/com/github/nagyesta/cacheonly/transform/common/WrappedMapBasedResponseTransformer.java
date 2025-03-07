package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
public class WrappedMapBasedResponseTransformer<B, C extends Map<I, E>, E, I>
        extends AbstractWrappedMapBasedTransformer<B, C, E, I>
        implements BatchResponseTransformer<B, B, I> {

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param instanceSupplier   The {@link Supplier} we can use for getting a new empty batch instance.
     * @param mapReadFunction    The function that can read the map from a batch.
     * @param mapWriteBiFunction The function that can write the map into a batch.
     * @param mapCollector       The collector creating a new map from the partial entities.
     */
    public WrappedMapBasedResponseTransformer(
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
    public WrappedMapBasedResponseTransformer(
            final @NotNull Function<B, B> cloneFunction,
            final @NotNull Function<B, C> mapReadFunction,
            final @NotNull BiFunction<B, C, B> mapWriteBiFunction,
            final @NotNull Collector<Map.Entry<I, E>, ?, C> mapCollector) {
        super(cloneFunction, mapReadFunction, mapWriteBiFunction, mapCollector);
    }

    @NotNull
    @Override
    public Map<I, B> splitToPartialResponse(final @NotNull B batchResponse) {
        return splitToMap(batchResponse);
    }

    @Nullable
    @Override
    public B mergeToBatchResponse(final @NotNull Map<I, B> entityMap) {
        return mergeToBatch(entityMap);
    }

}
