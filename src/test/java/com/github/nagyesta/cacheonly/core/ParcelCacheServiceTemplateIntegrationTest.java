package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.example.parcel.ParcelCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.parcel.ParcelContext;
import com.github.nagyesta.cacheonly.example.parcel.raw.ParcelBatchServiceCaller;
import com.github.nagyesta.cacheonly.example.parcel.raw.ParcelService;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ParcelContext.class)
@SuppressWarnings("checkstyle:MagicNumber")
class ParcelCacheServiceTemplateIntegrationTest {

    @Autowired
    private ParcelCacheServiceTemplate underTest;
    @Autowired
    private ParcelBatchServiceCaller batchServiceCaller;
    @Autowired
    private ParcelService parcelService;

    private static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyList(), 0))
                .add(Arguments.of(Collections.singletonList("A1234"), 1))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4009)
                        .mapToObj(i -> "B" + i)
                        .collect(Collectors.toList()), 1))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4021)
                        .mapToObj(i -> "C" + i)
                        .collect(Collectors.toList()), 3))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4100)
                        .mapToObj(i -> "D" + i)
                        .collect(Collectors.toList()), 10))
                .add(Arguments.of(IntStream.rangeClosed(4001, 4100)
                        .filter(i -> i % 5 == 0)
                        .mapToObj(i -> "B" + i)
                        .collect(Collectors.toList()), 2))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testCallCacheableBatchServiceShouldBeSplittingRequestsWhenItHasMoreItemsThanAllowed(
            final List<String> ids, final int expectedCalls) throws BatchServiceException {
        //given
        // reset spy
        final ParcelService spyService = batchServiceCaller.getParcelService();
        reset(spyService);
        // create the expected test response
        final List<ParcelResponse> expected = ids.stream()
                .sorted()
                .map(id -> new ParcelResponse(id, spyService.lookup(id)))
                .collect(Collectors.toList());

        //when
        final List<ParcelResponse> actual = underTest.callCacheableBatchService(ids);

        //then
        assertNotNull(actual);
        assertIterableEquals(expected, actual);
        // the real service was called the right amount of times
        verify(spyService, times(expectedCalls)).lookup(anyList());
    }
}
