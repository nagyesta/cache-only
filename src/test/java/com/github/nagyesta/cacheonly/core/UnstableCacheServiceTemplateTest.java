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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("checkstyle:MagicNumber")
class UnstableCacheServiceTemplateTest {

    @BeforeAll
    static void beforeAll() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UnstableCacheServiceTemplate.class)).setLevel(Level.DEBUG);
    }

    @Test
    public void testExceptionsAreHandledWhenBothCacheAndOriginFails() {
        //given
        final UnstableCacheServiceTemplate underTest = new UnstableCacheServiceTemplate();
        final BasicBatchServiceCallMetricCollector metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final List<Long> input = Arrays.asList(0L, 1L, 2L, 3L, 4L);

        //when
        final List<String> actual = underTest.callCacheableBatchService(input);

        //then
        assertNull(actual);
        assertEquals(metricCollector.getCacheGet(), 5);
        assertEquals(metricCollector.getCacheMiss(), 5);
        assertEquals(metricCollector.getCacheHit(), 0);
        assertEquals(metricCollector.getPartitionCreated(), 1);
        assertEquals(metricCollector.getPartitionFailed(), 0);
        assertEquals(metricCollector.getPartitionSucceeded(), 1);
    }

    @Test
    public void testExceptionsAreHandledWhenCalledWithEmptyList() {
        //given
        final UnstableCacheServiceTemplate underTest = new UnstableCacheServiceTemplate();
        final BasicBatchServiceCallMetricCollector metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final List<Long> input = Collections.emptyList();

        //when
        final List<String> actual = underTest.callCacheableBatchService(input);

        //then
        assertNull(actual);
        assertEquals(metricCollector.getCacheGet(), 0);
        assertEquals(metricCollector.getCacheMiss(), 0);
        assertEquals(metricCollector.getCacheHit(), 0);
        assertEquals(metricCollector.getPartitionCreated(), 0);
        assertEquals(metricCollector.getPartitionFailed(), 0);
        assertEquals(metricCollector.getPartitionSucceeded(), 0);
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.MILLISECONDS)
    public void testExceptionsAreHandledWhenBothCacheAndOriginTimesOut() {
        //given
        final UnstableCacheServiceTemplate underTest = new UnstableCacheServiceTemplate();
        final BasicBatchServiceCallMetricCollector metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final List<Long> input = Arrays.asList(-40L, -31L, -32L, -33L, -44L);

        //when
        assertThrows(BatchServiceException.class, () -> underTest.callCacheableBatchService(input));

        //then + exception
        assertEquals(metricCollector.getCacheGet(), 5);
        assertEquals(metricCollector.getCacheMiss(), 5);
        assertEquals(metricCollector.getCacheHit(), 0);
        assertEquals(metricCollector.getPartitionCreated(), 1);
        assertEquals(metricCollector.getPartitionFailed(), 1);
        assertEquals(metricCollector.getPartitionSucceeded(), 0);
    }

    @Test
    public void testExceptionsAreHandledWhenCallsResultErrors() {
        //given
        final UnstableCacheServiceTemplate underTest = new UnstableCacheServiceTemplate();
        final BasicBatchServiceCallMetricCollector metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final List<Long> input = Collections.singletonList(-15L);

        //when
        assertThrows(BatchServiceException.class, () -> underTest.callCacheableBatchService(input));

        //then + exception
        assertEquals(metricCollector.getCacheGet(), 1);
        assertEquals(metricCollector.getCacheMiss(), 1);
        assertEquals(metricCollector.getCacheHit(), 0);
        assertEquals(metricCollector.getPartitionCreated(), 1);
        assertEquals(metricCollector.getPartitionFailed(), 1);
        assertEquals(metricCollector.getPartitionSucceeded(), 0);
    }

    @Test
    public void testHappyCaseIsWorkingWhenFoundInCache() {
        //given
        final UnstableCacheServiceTemplate underTest = new UnstableCacheServiceTemplate();
        final BasicBatchServiceCallMetricCollector metricCollector = new BasicBatchServiceCallMetricCollector();
        underTest.setMetricsCollector(metricCollector);

        final List<Long> input = Collections.singletonList(19L);

        //when
        final List<String> actual = underTest.callCacheableBatchService(input);

        //then
        assertNotNull(actual);
        assertIterableEquals(Collections.singletonList("19"), actual);
        assertEquals(metricCollector.getCacheGet(), 1);
        assertEquals(metricCollector.getCacheMiss(), 0);
        assertEquals(metricCollector.getCacheHit(), 1);
        assertEquals(metricCollector.getPartitionCreated(), 0);
        assertEquals(metricCollector.getPartitionFailed(), 0);
        assertEquals(metricCollector.getPartitionSucceeded(), 0);
    }
}
