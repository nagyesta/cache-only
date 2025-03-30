package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.CollectionWrapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SuppressWarnings("checkstyle:MagicNumber")
class WrappedCollectionBasedRequestTransformerTest {

    private static final CollectionWrapper<String> OUT_OF_ORDER_BATCH = new CollectionWrapper<>(Stream.of(1L, 42L, 3L)
            .map(String::valueOf)
            .toList());
    private static final Map<Long, CollectionWrapper<String>> OUT_OF_ORDER_MAP = Stream.of(1L, 42L, 3L)
            .collect(Collectors.toMap(Function.identity(),
                    l -> new CollectionWrapper<>(Collections.singletonList(String.valueOf(l)))));
    private static final CollectionWrapper<String> ORDERED_BATCH = new CollectionWrapper<>(LongStream.range(0L, 30L).boxed()
            .map(String::valueOf)
            .toList());
    private static final Map<Long, CollectionWrapper<String>> ORDERED_MAP = LongStream.range(0L, 30L).boxed()
            .collect(Collectors.toMap(Function.identity(),
                    l -> new CollectionWrapper<>(Collections.singletonList(String.valueOf(l)))));

    private static Stream<Arguments> splitInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(new CollectionWrapper<String>(Collections.emptyList()), Collections.emptyMap()))
                .add(Arguments.of(OUT_OF_ORDER_BATCH, OUT_OF_ORDER_MAP))
                .add(Arguments.of(ORDERED_BATCH, ORDERED_MAP))
                .build();
    }

    private static Stream<Arguments> mergeInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyMap(), null))
                .add(Arguments.of(OUT_OF_ORDER_MAP, OUT_OF_ORDER_BATCH))
                .add(Arguments.of(ORDERED_MAP, ORDERED_BATCH))
                .build();
    }

    @ParameterizedTest
    @MethodSource("splitInputProvider")
    void testSplitToPartialRequestShouldSplitValidInput(
            final CollectionWrapper<String> input,
            final Map<Long, CollectionWrapper<String>> expected) {
        //given
        final var underTest =
                new WrappedCollectionBasedRequestTransformer<CollectionWrapper<String>, Collection<String>, String, Long>(
                        () -> new CollectionWrapper<>(new ArrayList<>()),
                        CollectionWrapper::getCollection,
                        (wrapper, collection) -> {
                            wrapper.setCollection(collection);
                            return wrapper;
                        }, Collectors.toCollection(ArrayList::new), Long::parseLong);

        //when
        final var actual = underTest.splitToPartialRequest(input);

        //then
        assertIterableEquals(expected.entrySet(), actual.entrySet());
    }

    @ParameterizedTest
    @MethodSource("mergeInputProvider")
    void testMergeToBatchRequestShouldMergeValidInput(
            final Map<Long, CollectionWrapper<String>> input,
            final CollectionWrapper<String> expected) {
        //given
        final var underTest =
                new WrappedCollectionBasedRequestTransformer<CollectionWrapper<String>, Collection<String>, String, Long>(
                        w -> new CollectionWrapper<>(new ArrayList<>()),
                        CollectionWrapper::getCollection,
                        (wrapper, collection) -> {
                            wrapper.setCollection(collection);
                            return wrapper;
                        }, Collectors.toCollection(ArrayList::new), Long::parseLong);

        //when
        final var actual = underTest.mergeToBatchRequest(input);

        //then
        assertEquals(expected, actual);
    }
}
