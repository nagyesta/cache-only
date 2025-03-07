package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Transformer implementation suitable for batches which are using a wrapper class around
 * a {@link Collection} of partial entities (and potentially other fields as well).
 *
 * @param <B> The type of the batch wrapper class.
 * @param <C> The type of the {@link Collection} holding the values we want to partition.
 * @param <E> The type of the partial entities.
 * @param <I> The type of the ID we can use for partial entity identification.
 */
public class WrappedCollectionBasedResponseTransformer<B, C extends Collection<E>, E, I>
        extends AbstractWrappedCollectionBasedTransformer<B, C, E, I>
        implements BatchResponseTransformer<B, B, I> {

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param instanceSupplier          The {@link Supplier} we can use for getting a new empty batch instance.
     * @param collectionReadFunction    The function that can read the collection from a batch.
     * @param collectionWriteBiFunction The function that can write the collection into a batch.
     * @param collectionCollector       The collector creating a new collection from the partial entities.
     * @param idFunction                The function that can convert an entity to the ID identifying it.
     */
    public WrappedCollectionBasedResponseTransformer(
            final @NotNull Supplier<B> instanceSupplier,
            final @NotNull Function<B, C> collectionReadFunction,
            final @NotNull BiFunction<B, C, B> collectionWriteBiFunction,
            final @NotNull Collector<E, ?, C> collectionCollector,
            final @NotNull Function<E, I> idFunction) {
        super(instanceSupplier, collectionReadFunction, collectionWriteBiFunction, collectionCollector, idFunction);
    }

    /**
     * Creates a new instance and sets all the parameters we can use for customization.
     *
     * @param cloneFunction             The function that can clone a batch.
     * @param collectionReadFunction    The function that can read the collection from a batch.
     * @param collectionWriteBiFunction The function that can write the collection into a batch.
     * @param collectionCollector       The collector creating a new collection from the partial entities.
     * @param idFunction                The function that can convert an entity to the ID identifying it.
     */
    public WrappedCollectionBasedResponseTransformer(
            final @NotNull Function<B, B> cloneFunction,
            final @NotNull Function<B, C> collectionReadFunction,
            final @NotNull BiFunction<B, C, B> collectionWriteBiFunction,
            final @NotNull Collector<E, ?, C> collectionCollector,
            final @NotNull Function<E, I> idFunction) {
        super(cloneFunction, collectionReadFunction, collectionWriteBiFunction, collectionCollector, idFunction);
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
