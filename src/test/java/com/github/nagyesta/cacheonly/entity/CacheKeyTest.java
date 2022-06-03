package com.github.nagyesta.cacheonly.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheKeyTest {

    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";

    @SuppressWarnings({"UnnecessaryBoxing", "checkstyle:MagicNumber"})
    private static Stream<Arguments> keyPairProvider() {
        final CacheKey<String, Integer> aCacheKey = new CacheKey<>(KEY_1, 1);
        return Stream.<Arguments>builder()
                .add(Arguments.of(aCacheKey, aCacheKey, true))
                .add(Arguments.of(new CacheKey<>(KEY_1, 1), new CacheKey<>(KEY_2, 1), false))
                .add(Arguments.of(new CacheKey<>(KEY_2, 1), new CacheKey<>(KEY_2, 2), false))
                .add(Arguments.of(new CacheKey<>(KEY_1, Integer.valueOf(23432)), new CacheKey<>(KEY_1, Integer.valueOf(23432)), true))
                .add(Arguments.of(new CacheKey<>(KEY_1, 1), KEY_1, false))
                .build();
    }

    @ParameterizedTest
    @MethodSource("keyPairProvider")
    void testEqualsShouldCompareBothFieldsWhenCalled(final Object a, final Object b, final boolean expected) {
        //given

        //when
        final boolean actual = a.equals(b);

        //then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("keyPairProvider")
    void testHashCodeShouldCompareBothFieldsWhenCalled(final Object a, final Object b, final boolean expected) {
        //given

        //when
        final int hashA = a.hashCode();
        final int hashB = b.hashCode();

        //then
        assertEquals(expected, hashA == hashB);
    }
}
