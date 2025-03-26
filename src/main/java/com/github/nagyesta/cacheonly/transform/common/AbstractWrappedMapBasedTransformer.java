package com.github.nagyesta.cacheonly.transform.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transformer implementation suitable for batches which are using a wrapper class around
 * a {@link Map} of partial entities (and potentially other fields as well).
 *
 * @param <B> The type of the batch wrapper class.
 * @param <C> The type of the {@link Map} holding the values we want to partition.
 * @param <E> The type of the partial entities.
 * @param <I> The type of the ID we can use for partial entity identification.
 */
public abstract class AbstractWrappedMapBasedTransformer<B, C extends Map<I, E>, E, I> {

    private final UnaryOperator<B> cloneFunction;
    private final Function<B, C> mapReadFunction;
    private final BiFunction<B, C, B> mapWriteBiFunction;
    private final Collector<Map.Entry<I, E>, ?, C> mapCollector;

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param instanceSupplier   The {@link Supplier} we can use for getting a new empty batch instance.
     * @param mapReadFunction    The function that can read the map from a batch.
     * @param mapWriteBiFunction The function that can write the map into a batch.
     * @param mapCollector       The collector creating a new map from the partial entities.
     */
    protected AbstractWrappedMapBasedTransformer(
            final @NotNull Supplier<B> instanceSupplier,
            final @NotNull Function<B, C> mapReadFunction,
            final @NotNull BiFunction<B, C, B> mapWriteBiFunction,
            final @NotNull Collector<Map.Entry<I, E>, ?, C> mapCollector) {
        this(request -> cloneWrapper(request, instanceSupplier), mapReadFunction, mapWriteBiFunction, mapCollector);
    }

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param cloneFunction      The function that can clone a batch.
     * @param mapReadFunction    The function that can read the map from a batch.
     * @param mapWriteBiFunction The function that can write the map into a batch.
     * @param mapCollector       The collector creating a new map from the partial entities.
     */
    protected AbstractWrappedMapBasedTransformer(
            final @NotNull UnaryOperator<B> cloneFunction,
            final @NotNull Function<B, C> mapReadFunction,
            final @NotNull BiFunction<B, C, B> mapWriteBiFunction,
            final @NotNull Collector<Map.Entry<I, E>, ?, C> mapCollector) {
        this.cloneFunction = cloneFunction;
        this.mapReadFunction = mapReadFunction;
        this.mapWriteBiFunction = mapWriteBiFunction;
        this.mapCollector = mapCollector;
    }

    @NotNull
    private static <B> B cloneWrapper(
            final @NotNull B batch,
            final @NotNull Supplier<B> instanceSupplier) {
        final var target = instanceSupplier.get();
        BeanUtils.copyProperties(batch, target);
        return target;
    }

    @NotNull
    protected final Map<I, B> splitToMap(final @NotNull B batch) {
        return mapReadFunction.apply(batch).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> mapWriteBiFunction.apply(cloneFunction.apply(batch), Stream.of(entry)
                                .collect(mapCollector))));
    }

    @Nullable
    protected final B mergeToBatch(final @NotNull Map<I, B> map) {
        return map.values().stream().findFirst()
                .map(request -> mapWriteBiFunction.apply(cloneFunction.apply(request), map.values().stream()
                        .map(mapReadFunction)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .collect(mapCollector)))
                .orElse(null);
    }

}
