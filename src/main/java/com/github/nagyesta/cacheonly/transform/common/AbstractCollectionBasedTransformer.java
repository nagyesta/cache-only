package com.github.nagyesta.cacheonly.transform.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Abstract transformer intended to be used in cases when the batch request (or response)
 * is a simple {@link Collection} of the partial requests (or responses).
 *
 * @param <C> The {@link Collection} type used for the batch.
 * @param <P> The type of the partial request (or response).
 * @param <I> The key type we want to use in the map.
 */
public class AbstractCollectionBasedTransformer<C extends Collection<P>, P, I> {

    private final Collector<P, ?, C> collectionCollector;
    private final Function<P, I> idFunction;
    private final boolean nullIfEmpty;

    /**
     * Creates a new instance and defines how the {@link Collection} should be collected from a {@link java.util.stream.Stream}.
     *
     * @param collectionCollector The {@link Collector} we want to use to get a batch from a stream of elements.
     * @param idFunction          The transformation that can determine the Id of a given partial request (or response).
     */
    public AbstractCollectionBasedTransformer(final @NotNull Collector<P, ?, C> collectionCollector,
                                              final @NotNull Function<P, I> idFunction) {
        this(collectionCollector, idFunction, false);
    }

    /**
     * Creates a new instance and defines how the {@link Collection} should be collected from a {@link java.util.stream.Stream}.
     *
     * @param collectionCollector The {@link Collector} we want to use to get a batch from a stream of elements.
     * @param idFunction          The transformation that can determine the Id of a given partial request (or response).
     * @param nullIfEmpty         Flag telling the implementation whether we want to use null in case of an empty {@link Collection}.
     */
    public AbstractCollectionBasedTransformer(final @NotNull Collector<P, ?, C> collectionCollector,
                                              final @NotNull Function<P, I> idFunction,
                                              final boolean nullIfEmpty) {
        this.collectionCollector = collectionCollector;
        this.idFunction = idFunction;
        this.nullIfEmpty = nullIfEmpty;
    }

    @NotNull
    protected final Map<I, P> splitToMap(final @NotNull C batch) {
        return batch.stream()
                .collect(Collectors.toMap(idFunction, Function.identity()));
    }

    @Nullable
    protected final C mergeToBatch(final @NotNull Map<I, P> map) {
        if (map.isEmpty() && nullIfEmpty) {
            return null;
        }
        return map.values().stream().collect(collectionCollector);
    }


}
