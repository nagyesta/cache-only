package com.github.nagyesta.cacheonly.core;

import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static com.github.nagyesta.cacheonly.core.CacheRefreshStrategy.CACHE_ONLY;
import static com.github.nagyesta.cacheonly.core.CacheRefreshStrategy.NEVER_CACHE;
import static com.github.nagyesta.cacheonly.core.CacheRefreshStrategy.OPPORTUNISTIC;
import static com.github.nagyesta.cacheonly.core.CacheRefreshStrategy.OPTIMISTIC;
import static com.github.nagyesta.cacheonly.core.CacheRefreshStrategy.PESSIMISTIC;
import static com.github.nagyesta.cacheonly.core.CacheRefreshStrategy.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit test for {@link CacheRefreshStrategy}.
 */
class CacheRefreshStrategyTest {

    private static final Set<Integer> INTEGERS_3_TO_6 = SetUtils.unmodifiableSet(
            SetUtils.hashSet(3, 4, 5, 6));
    private static final Set<Integer> INTEGERS_3_TO_6_SORTED = SetUtils.unmodifiableSortedSet(
            new TreeSet<>(INTEGERS_3_TO_6));
    private static final Set<Integer> INTEGERS_1_TO_8 = SetUtils.unmodifiableSet(
            SetUtils.hashSet(1, 2, 3, 4, 5, 6, 7, 8));
    private static final Set<Integer> INTEGERS_1_TO_8_SORTED = SetUtils.unmodifiableSortedSet(
            new TreeSet<>(INTEGERS_1_TO_8));
    private static final Set<Integer> INTEGERS_1_2_7_8_SORTED = SetUtils.unmodifiableSortedSet(
            new TreeSet<>(SetUtils.difference(INTEGERS_1_TO_8, INTEGERS_3_TO_6)));
    private static final SortedSet<Object> EMPTY = Collections.emptySortedSet();
    private static final int ZERO = 0;
    private static final int PARTITION_SIZE_2 = 2;
    private static final int PARTITION_SIZE_3 = 2;
    private static final int EXTRA_2 = 2;
    private static final Set<Integer> NULL_VALUE_IN_SET = Collections.singleton(null);

    private static Stream<Arguments> cacheUseProvider() {
        return Stream.<Arguments>builder()
                .add(of(CACHE_ONLY, true))
                .add(of(OPTIMISTIC, true))
                .add(of(OPPORTUNISTIC, true))
                .add(of(PESSIMISTIC, true))
                .add(of(NEVER_CACHE, false))
                .build();
    }

    private static Stream<Arguments> failOnMissProvider() {
        return Stream.<Arguments>builder()
                .add(of(CACHE_ONLY, false))
                .add(of(OPTIMISTIC, false))
                .add(of(OPPORTUNISTIC, false))
                .add(of(PESSIMISTIC, true))
                .add(of(NEVER_CACHE, false))
                .build();
    }

    private static Stream<Arguments> validSelectItemProvider() {
        final Stream.Builder<Arguments> builder = Stream.builder();
        // all found
        Arrays.stream(values()).forEach(strategy -> {
            builder.add(of(strategy, EMPTY, EMPTY, PARTITION_SIZE_2, EMPTY, ZERO));
            builder.add(of(strategy, INTEGERS_1_TO_8, INTEGERS_1_TO_8_SORTED, PARTITION_SIZE_2, EMPTY, ZERO));
        });
        // non found
        builder.add(of(NEVER_CACHE, INTEGERS_1_TO_8, EMPTY, PARTITION_SIZE_3, INTEGERS_1_TO_8_SORTED, ZERO));
        builder.add(of(CACHE_ONLY, INTEGERS_1_TO_8, EMPTY, PARTITION_SIZE_3, EMPTY, ZERO));
        builder.add(of(PESSIMISTIC, INTEGERS_1_TO_8, EMPTY, PARTITION_SIZE_3, INTEGERS_1_TO_8_SORTED, ZERO));
        // found middle half
        builder.add(of(OPTIMISTIC, INTEGERS_1_TO_8, INTEGERS_3_TO_6, PARTITION_SIZE_3, INTEGERS_1_2_7_8_SORTED, ZERO));
        builder.add(of(OPPORTUNISTIC, INTEGERS_1_TO_8, INTEGERS_3_TO_6, PARTITION_SIZE_3, INTEGERS_1_2_7_8_SORTED, EXTRA_2));
        builder.add(of(PESSIMISTIC, INTEGERS_1_TO_8, INTEGERS_3_TO_6, PARTITION_SIZE_3, INTEGERS_1_TO_8_SORTED, ZERO));
        // found tail ends
        builder.add(of(OPTIMISTIC, INTEGERS_1_TO_8, INTEGERS_1_2_7_8_SORTED, PARTITION_SIZE_3, INTEGERS_3_TO_6_SORTED, ZERO));
        builder.add(of(OPPORTUNISTIC, INTEGERS_1_TO_8, INTEGERS_1_2_7_8_SORTED, PARTITION_SIZE_3, INTEGERS_3_TO_6_SORTED, EXTRA_2));
        builder.add(of(PESSIMISTIC, INTEGERS_1_TO_8, INTEGERS_1_2_7_8_SORTED, PARTITION_SIZE_3, INTEGERS_1_TO_8_SORTED, ZERO));
        return builder.build();
    }

    private static Stream<Arguments> invalidSelectItemProvider() {
        final Stream.Builder<Arguments> builder = Stream.builder();
        Arrays.stream(values()).forEach(strategy -> {
            // null as set
            builder.add(of(strategy, null, null, PARTITION_SIZE_2));
            builder.add(of(strategy, null, EMPTY, PARTITION_SIZE_2));
            builder.add(of(strategy, EMPTY, null, PARTITION_SIZE_2));
            // invalid partition size
            builder.add(of(strategy, EMPTY, EMPTY, ZERO));
            builder.add(of(strategy, null, EMPTY, ZERO));
            builder.add(of(strategy, EMPTY, null, ZERO));
            // null value in set
            builder.add(of(strategy, NULL_VALUE_IN_SET, EMPTY, PARTITION_SIZE_3));
            builder.add(of(strategy, EMPTY, NULL_VALUE_IN_SET, PARTITION_SIZE_3));
            // has items in cache but not in request
            builder.add(of(strategy, EMPTY, INTEGERS_3_TO_6, PARTITION_SIZE_3));
        });
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("cacheUseProvider")
    void testAllowsCacheGetShouldReturnTheExpectedValueWhenCalled(
            final CacheRefreshStrategy underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.allowsCacheGet();

        //then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("failOnMissProvider")
    void testShouldFailOnMissShouldReturnTheExpectedValueWhenCalled(
            final CacheRefreshStrategy underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.shouldFailOnMiss();

        //then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("cacheUseProvider")
    void testAllowsCachePutShouldReturnTheExpectedValueWhenCalled(
            final CacheRefreshStrategy underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.allowsCachePut();

        //then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("validSelectItemProvider")
    void testSelectItemsForFetchShouldKeepTheExpectedItemsWhenCalledWithValidInput(
            final CacheRefreshStrategy underTest,
            final Set<Integer> allIds, final Set<Integer> foundIds, final int partitionSize,
            final SortedSet<Integer> expectedMandatory, final int expectedAdditional) {
        //given

        //when
        final Set<Integer> actual = underTest.selectItemsForFetch(allIds, foundIds, partitionSize);

        //then
        final SortedSet<Integer> common = new TreeSet<>(SetUtils.intersection(actual, expectedMandatory));
        final Set<Integer> extra = SetUtils.difference(actual, expectedMandatory);
        assertIterableEquals(expectedMandatory, common);
        assertEquals(expectedAdditional, extra.size());
        assertTrue(foundIds.containsAll(extra));
    }


    @ParameterizedTest
    @MethodSource("invalidSelectItemProvider")
    void testSelectItemsForFetchShouldThrowExceptionWhenCalledWithInvalidInput(
            final CacheRefreshStrategy underTest,
            final Set<Integer> allIds, final Set<Integer> foundIds, final int partitionSize) {
        //given

        //when
        assertThrows(IllegalArgumentException.class,
                () -> underTest.selectItemsForFetch(allIds, foundIds, partitionSize));

        //then exception
    }
}
