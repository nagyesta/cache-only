package com.github.nagyesta.cacheonly.transform.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SuppressWarnings("checkstyle:MagicNumber")
class MapBasedResponseTransformerTest {

    private static final SortedMap<Long, String> OUT_OF_ORDER_BATCH = Stream.of(1L, 42L, 3L)
            .collect(Collectors.toMap(Function.identity(), String::valueOf, (t, u) -> t, TreeMap::new));
    private static final Map<Long, String> OUT_OF_ORDER_MAP = Stream.of(1L, 42L, 3L)
            .collect(Collectors.toMap(Function.identity(), String::valueOf));
    private static final SortedMap<Long, String> ORDERED_BATCH = LongStream.range(0L, 30L).boxed()
            .collect(Collectors.toMap(Function.identity(), String::valueOf, (t, u) -> t, TreeMap::new));
    private static final Map<Long, String> ORDERED_MAP = LongStream.range(0L, 30L).boxed()
            .collect(Collectors.toMap(Function.identity(), String::valueOf));

    private static Stream<Arguments> splitInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptySortedMap(), Collections.emptyMap()))
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
    void testSplitToPartialResponseShouldSplitValidInput(
            final SortedMap<Long, String> input,
            final Map<Long, String> expected) {
        //given
        final var underTest =
                new MapBasedResponseTransformer<SortedMap<Long, String>, String, Long>(TreeMap::new);

        //when
        final var actual = underTest.splitToPartialResponse(input);

        //then
        assertIterableEquals(expected.entrySet(), actual.entrySet());
    }

    @ParameterizedTest
    @MethodSource("mergeInputProvider")
    void testMergeToBatchResponseShouldMergeValidInput(
            final Map<Long, String> input,
            final SortedMap<Long, String> expected) {
        //given
        final var underTest =
                new MapBasedResponseTransformer<SortedMap<Long, String>, String, Long>(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (t, u) -> t, TreeMap::new),
                        Map.Entry::getKey,
                        Map.Entry::getValue, true);

        //when
        final var actual = underTest.mergeToBatchResponse(input);

        //then
        assertEquals(expected, actual);
    }
}
