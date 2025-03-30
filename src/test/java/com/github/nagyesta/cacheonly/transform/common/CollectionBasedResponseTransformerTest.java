package com.github.nagyesta.cacheonly.transform.common;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("checkstyle:MagicNumber")
class CollectionBasedResponseTransformerTest {

    private static final List<String> OUT_OF_ORDER_BATCH = Stream.of(1L, 42L, 3L)
            .map(String::valueOf)
            .toList();
    private static final Map<Long, String> OUT_OF_ORDER_MAP = Stream.of(1L, 42L, 3L)
            .collect(Collectors.toMap(Function.identity(), String::valueOf));
    private static final List<String> ORDERED_BATCH = LongStream.range(0L, 30L).boxed()
            .map(String::valueOf)
            .toList();
    private static final Map<Long, String> ORDERED_MAP = LongStream.range(0L, 30L).boxed()
            .collect(Collectors.toMap(Function.identity(), String::valueOf));

    private static Stream<Arguments> splitInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyList(), Collections.emptyMap()))
                .add(Arguments.of(OUT_OF_ORDER_BATCH, OUT_OF_ORDER_MAP))
                .add(Arguments.of(ORDERED_BATCH, ORDERED_MAP))
                .build();
    }

    private static Stream<Arguments> mergeInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyMap(), Collections.emptyList()))
                .add(Arguments.of(OUT_OF_ORDER_MAP, OUT_OF_ORDER_BATCH))
                .add(Arguments.of(ORDERED_MAP, ORDERED_BATCH))
                .build();
    }

    @ParameterizedTest
    @MethodSource("splitInputProvider")
    void testSplitToPartialResponseShouldSplitValidInput(
            final List<String> input,
            final Map<Long, String> expected) {
        //given
        final var underTest =
                new CollectionBasedResponseTransformer<List<String>, String, Long>(
                        Collectors.toList(), Long::parseLong);

        //when
        final var actual = underTest.splitToPartialResponse(input);

        //then
        assertIterableEquals(expected.entrySet(), actual.entrySet());
    }

    @ParameterizedTest
    @MethodSource("mergeInputProvider")
    void testMergeToBatchResponseShouldMergeValidInput(
            final Map<Long, String> input,
            final List<String> expected) {
        //given
        final var underTest =
                new CollectionBasedResponseTransformer<List<String>, String, Long>(
                        Collectors.toList(), Long::parseLong, false);

        //when
        final var actual = underTest.mergeToBatchResponse(input);

        //then
        assertNotNull(actual);
        assertTrue(CollectionUtils.containsAll(expected, actual));
        assertTrue(CollectionUtils.containsAll(actual, expected));
    }
}
