package com.github.nagyesta.cacheonly.transform.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Abstract transformer intended to be used in cases when the batch request (or response)
 * is a simple {@link Map} of the partial requests (or responses) using the Id as key.
 *
 * @param <C> The {@link Map} type used for the batch.
 * @param <P> The type of the partial request (or response) payload.
 * @param <I> The key type we want to use in the map.
 */
public class AbstractMapBasedTransformer<C extends Map<I, P>, P, I> {

    private final Collector<Map.Entry<I, P>, ?, C> mergeMapCollector;
    private final Function<Map.Entry<I, P>, I> splitKeyTransformer;
    private final Function<Map.Entry<I, P>, P> splitValueTransformer;
    private final boolean nullIfEmpty;

    /**
     * Creates a new instance and defines the Map collector we want to use when we have the entry set.
     *
     * @param mergeMapCollector The collector we want to use when we merge partials to a batch.
     */
    public AbstractMapBasedTransformer(final @NotNull Collector<Map.Entry<I, P>, ?, C> mergeMapCollector) {
        this(mergeMapCollector, Map.Entry::getKey, Map.Entry::getValue, false);
    }

    /**
     * Creates a new instance and defines all parameters we can use for customization.
     *
     * @param mergeMapCollector     The collector we want to use when we merge partials to a batch.
     * @param splitKeyTransformer   The Function we need to use when we split the batch for transforming
     *                              an Entry to a key in the partial entry.
     * @param splitValueTransformer The Function we need to use when we split the batch for transforming
     *                              an Entry to a value in the partial entry.
     * @param nullIfEmpty           True is we need to return null in case we are merging an empty map.
     */
    public AbstractMapBasedTransformer(final @NotNull Collector<Map.Entry<I, P>, ?, C> mergeMapCollector,
                                       final @NotNull Function<Map.Entry<I, P>, I> splitKeyTransformer,
                                       final @NotNull Function<Map.Entry<I, P>, P> splitValueTransformer,
                                       final boolean nullIfEmpty) {
        this.mergeMapCollector = mergeMapCollector;
        this.splitKeyTransformer = splitKeyTransformer;
        this.splitValueTransformer = splitValueTransformer;
        this.nullIfEmpty = nullIfEmpty;
    }

    @NotNull
    protected final Map<I, P> splitToMap(final @NotNull C batch) {
        return batch.entrySet().stream()
                .collect(Collectors.toMap(splitKeyTransformer, splitValueTransformer));
    }

    @Nullable
    protected final C mergeToBatch(final @NotNull Map<I, P> map) {
        if (map.isEmpty() && nullIfEmpty) {
            return null;
        }
        return map.entrySet().stream().collect(mergeMapCollector);
    }


}
