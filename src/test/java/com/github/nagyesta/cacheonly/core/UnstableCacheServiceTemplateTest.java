package com.github.nagyesta.cacheonly.core;

import ch.qos.logback.classic.Level;
import com.github.nagyesta.cacheonly.core.metrics.BasicBatchServiceCallMetricCollector;
import com.github.nagyesta.cacheonly.example.unstable.UnstableCacheServiceTemplate;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("checkstyle:MagicNumber")
class UnstableCacheServiceTemplateTest {

    @BeforeAll
    static void beforeAll() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UnstableCacheServiceTemplate.class)).setLevel(Level.DEBUG);
    }

    @Test
    public void testExceptionsAreHandledWhenBothCacheAndOriginFails() {
        //given
        final var underTest = new UnstableCacheServiceTemplate();
        final var metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final var input = Arrays.asList(0L, 1L, 2L, 3L, 4L);

        //when
        final var actual = underTest.callCacheableBatchService(input);

        //then
        assertNull(actual);
        assertEquals(5, metricCollector.getCacheGet());
        assertEquals(5, metricCollector.getCacheMiss());
        assertEquals(0, metricCollector.getCacheHit());
        assertEquals(1, metricCollector.getPartitionCreated());
        assertEquals(0, metricCollector.getPartitionFailed());
        assertEquals(1, metricCollector.getPartitionSucceeded());
    }

    @Test
    public void testExceptionsAreHandledWhenCalledWithEmptyList() {
        //given
        final var underTest = new UnstableCacheServiceTemplate();
        final var metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final List<Long> input = Collections.emptyList();

        //when
        final var actual = underTest.callCacheableBatchService(input);

        //then
        assertNull(actual);
        assertEquals(0, metricCollector.getCacheGet());
        assertEquals(0, metricCollector.getCacheMiss());
        assertEquals(0, metricCollector.getCacheHit());
        assertEquals(0, metricCollector.getPartitionCreated());
        assertEquals(0, metricCollector.getPartitionFailed());
        assertEquals(0, metricCollector.getPartitionSucceeded());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.MILLISECONDS)
    public void testExceptionsAreHandledWhenBothCacheAndOriginTimesOut() {
        //given
        final var underTest = new UnstableCacheServiceTemplate();
        final var metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final var input = Arrays.asList(-40L, -31L, -32L, -33L, -44L);

        //when
        assertThrows(BatchServiceException.class, () -> underTest.callCacheableBatchService(input));

        //then + exception
        assertEquals(5, metricCollector.getCacheGet());
        assertEquals(5, metricCollector.getCacheMiss());
        assertEquals(0, metricCollector.getCacheHit());
        assertEquals(1, metricCollector.getPartitionCreated());
        assertEquals(1, metricCollector.getPartitionFailed());
        assertEquals(0, metricCollector.getPartitionSucceeded());
    }

    @Test
    public void testExceptionsAreHandledWhenCallsResultErrors() {
        //given
        final var underTest = new UnstableCacheServiceTemplate();
        final var metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final var input = Collections.singletonList(-15L);

        //when
        assertThrows(BatchServiceException.class, () -> underTest.callCacheableBatchService(input));

        //then + exception
        assertEquals(1, metricCollector.getCacheGet());
        assertEquals(1, metricCollector.getCacheMiss());
        assertEquals(0, metricCollector.getCacheHit());
        assertEquals(1, metricCollector.getPartitionCreated());
        assertEquals(1, metricCollector.getPartitionFailed());
        assertEquals(0, metricCollector.getPartitionSucceeded());
    }

    @Test
    public void testHappyCaseIsWorkingWhenFoundInCache() {
        //given
        final var underTest = new UnstableCacheServiceTemplate();
        final var metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final var input = Collections.singletonList(19L);

        //when
        final var actual = underTest.callCacheableBatchService(input);

        //then
        assertNotNull(actual);
        assertIterableEquals(Collections.singletonList("19"), actual);
        assertEquals(1, metricCollector.getCacheGet());
        assertEquals(0, metricCollector.getCacheMiss());
        assertEquals(1, metricCollector.getCacheHit());
        assertEquals(0, metricCollector.getPartitionCreated());
        assertEquals(0, metricCollector.getPartitionFailed());
        assertEquals(0, metricCollector.getPartitionSucceeded());
    }
}
