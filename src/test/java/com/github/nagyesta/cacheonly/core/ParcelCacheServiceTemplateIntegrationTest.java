package com.github.nagyesta.cacheonly.core;

import ch.qos.logback.classic.Level;
import com.github.nagyesta.cacheonly.example.parcel.ParcelCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.parcel.ParcelContext;
import com.github.nagyesta.cacheonly.example.parcel.raw.ParcelBatchServiceCaller;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ParcelContext.class)
@SuppressWarnings("checkstyle:MagicNumber")
class ParcelCacheServiceTemplateIntegrationTest {

    @Autowired
    private ParcelCacheServiceTemplate underTest;
    @Autowired
    private ParcelBatchServiceCaller batchServiceCaller;

    @BeforeAll
    static void beforeAll() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ParcelCacheServiceTemplate.class)).setLevel(Level.DEBUG);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.springframework")).setLevel(Level.WARN);
    }

    private static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyList(), 0))
                .add(Arguments.of(Collections.singletonList("A1234"), 1))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4009)
                        .mapToObj(i -> "B" + i)
                        .toList(), 1))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4021)
                        .mapToObj(i -> "C" + i)
                        .toList(), 3))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4100)
                        .mapToObj(i -> "D" + i)
                        .toList(), 10))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4100)
                        .filter(i -> i % 5 == 0)
                        .mapToObj(i -> "B" + i)
                        .toList(), 2))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testCallCacheableBatchServiceShouldBeSplittingRequestsWhenItHasMoreItemsThanAllowed(
            final List<String> ids,
            final int expectedCalls)
            throws BatchServiceException {
        //given
        // reset spy
        final var spyService = batchServiceCaller.getParcelService();
        reset(spyService);
        // create the expected test response
        final var expected = ids.stream()
                .sorted()
                .map(id -> new ParcelResponse(id, spyService.lookup(id)))
                .toList();

        //when
        final var actual = underTest.callCacheableBatchService(ids);

        //then
        if (expectedCalls == 0) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertIterableEquals(expected, actual);
        }
        // the real service was called the right amount of times
        verify(spyService, times(expectedCalls)).lookup(anyList());
    }
}
