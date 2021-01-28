package com.github.nagyesta.cacheonly.transform.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transformer implementation suitable for batches which are using a wrapper class around
 * a {@link Collection} of partial entities (and potentially other fields as well).
 *
 * @param <B> The type of the batch wrapper class.
 * @param <C> The type of the {@link Collection} holding the values we want to partition.
 * @param <E> The type of the partial entities.
 * @param <I> The type of the Id we can use for partial entity identification.
 */
public class AbstractWrappedCollectionBasedTransformer<B, C extends Collection<E>, E, I> {

    private final Function<B, B> cloneFunction;
    private final Function<B, C> collectionReadFunction;
    private final BiFunction<B, C, B> collectionWriteBiFunction;
    private final Collector<E, ?, C> collectionCollector;
    private final Function<E, I> idFunction;

    /**
     * Creates a new instance and sets all of the parameters we can use for customization.
     *
     * @param instanceSupplier          The {@link Supplier} we can use for getting a new empty batch instance.
     * @param collectionReadFunction    The function that can read the collection from a batch.
     * @param collectionWriteBiFunction The function that can write the collection into a batch.
     * @param collectionCollector       The collector creating a new collection from the partial entities.
     * @param idFunction                The function that can convert an entity to the Id identifying it.
     */
    public AbstractWrappedCollectionBasedTransformer(final @NotNull Supplier<B> instanceSupplier,
                                                     final @NotNull Function<B, C> collectionReadFunction,
                                                     final @NotNull BiFunction<B, C, B> collectionWriteBiFunction,
                                                     final @NotNull Collector<E, ?, C> collectionCollector,
                                                     final @NotNull Function<E, I> idFunction) {
        this(response -> cloneWrapper(response, instanceSupplier), collectionReadFunction,
                collectionWriteBiFunction, collectionCollector, idFunction);
    }

    /**
     * Creates a new instance and sets all of the parameters we can use for customization.
     *
     * @param cloneFunction             The function that can clone a batch.
     * @param collectionReadFunction    The function that can read the collection from a batch.
     * @param collectionWriteBiFunction The function that can write the collection into a batch.
     * @param collectionCollector       The collector creating a new collection from the partial entities.
     * @param idFunction                The function that can convert an entity to the Id identifying it.
     */
    public AbstractWrappedCollectionBasedTransformer(final @NotNull Function<B, B> cloneFunction,
                                                     final @NotNull Function<B, C> collectionReadFunction,
                                                     final @NotNull BiFunction<B, C, B> collectionWriteBiFunction,
                                                     final @NotNull Collector<E, ?, C> collectionCollector,
                                                     final @NotNull Function<E, I> idFunction) {
        this.cloneFunction = cloneFunction;
        this.collectionReadFunction = collectionReadFunction;
        this.collectionWriteBiFunction = collectionWriteBiFunction;
        this.collectionCollector = collectionCollector;
        this.idFunction = idFunction;
    }

    @NotNull
    private static <B> B cloneWrapper(final @NotNull B batch, final @NotNull Supplier<B> instanceSupplier) {
        final B target = instanceSupplier.get();
        BeanUtils.copyProperties(batch, target);
        return target;
    }

    @NotNull
    protected final Map<I, B> splitToMap(final @NotNull B batch) {
        return collectionReadFunction.apply(batch).stream()
                .collect(Collectors.toMap(idFunction,
                        entity -> collectionWriteBiFunction
                                .apply(cloneFunction.apply(batch), Stream.of(entity).collect(collectionCollector))));
    }

    @Nullable
    protected final B mergeToBatch(final @NotNull Map<I, B> map) {
        return map.values().stream().findFirst()
                .map(entity -> collectionWriteBiFunction.apply(cloneFunction.apply(entity),
                        map.values().stream()
                                .map(collectionReadFunction)
                                .flatMap(Collection::stream)
                                .collect(collectionCollector)))
                .orElse(null);
    }

}
