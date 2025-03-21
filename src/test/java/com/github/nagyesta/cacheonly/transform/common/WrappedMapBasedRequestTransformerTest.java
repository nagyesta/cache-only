package com.github.nagyesta.cacheonly.transform.common;

import com.github.nagyesta.cacheonly.transform.MapWrapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SuppressWarnings("checkstyle:MagicNumber")
class WrappedMapBasedRequestTransformerTest {

    private static final MapWrapper<Long, String> OUT_OF_ORDER_BATCH = new MapWrapper<>(Stream.of(1L, 42L, 3L)
            .collect(Collectors.toMap(Function.identity(), String::valueOf)));
    private static final Map<Long, MapWrapper<Long, String>> OUT_OF_ORDER_MAP = Stream.of(1L, 42L, 3L)
            .collect(Collectors.toMap(Function.identity(),
                    l -> new MapWrapper<>(Collections.singletonMap(l, String.valueOf(l)))));
    private static final MapWrapper<Long, String> ORDERED_BATCH = new MapWrapper<>(LongStream.range(0L, 30L).boxed()
            .collect(Collectors.toMap(Function.identity(), String::valueOf)));
    private static final Map<Long, MapWrapper<Long, String>> ORDERED_MAP = LongStream.range(0L, 30L).boxed()
            .collect(Collectors.toMap(Function.identity(),
                    l -> new MapWrapper<>(Collections.singletonMap(l, String.valueOf(l)))));

    private static Stream<Arguments> splitInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(new MapWrapper<Long, String>(Collections.emptyMap()), Collections.emptyMap()))
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
            final MapWrapper<Long, String> input,
            final Map<Long, MapWrapper<Long, String>> expected) {
        //given
        final var underTest =
                new WrappedMapBasedRequestTransformer<MapWrapper<Long, String>, Map<Long, String>, String, Long>(
                        () -> new MapWrapper<>(new HashMap<>()), MapWrapper::getMap, (wrapper, map) -> {
                    wrapper.setMap(map);
                    return wrapper;
                }, Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //when
        final var actual = underTest.splitToPartialRequest(input);

        //then
        assertIterableEquals(expected.entrySet(), actual.entrySet());
    }

    @ParameterizedTest
    @MethodSource("mergeInputProvider")
    void testMergeToBatchRequestShouldMergeValidInput(
            final Map<Long, MapWrapper<Long, String>> input,
            final MapWrapper<Long, String> expected) {
        //given
        final var underTest =
                new WrappedMapBasedRequestTransformer<MapWrapper<Long, String>, Map<Long, String>, String, Long>(
                        w -> new MapWrapper<>(new HashMap<>()),
                        MapWrapper::getMap,
                        (wrapper, map) -> {
                            wrapper.setMap(map);
                            return wrapper;
                        }, Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //when
        final var actual = underTest.mergeToBatchRequest(input);

        //then
        assertEquals(expected, actual);
    }
}
