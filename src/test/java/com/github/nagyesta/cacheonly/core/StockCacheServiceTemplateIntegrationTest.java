package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.example.stock.StockCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.stock.StockContext;
import com.github.nagyesta.cacheonly.example.stock.raw.StockBatchServiceCaller;
import com.github.nagyesta.cacheonly.example.stock.raw.StockService;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static com.github.nagyesta.cacheonly.example.stock.StockContext.STOCKS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StockContext.class)
@SuppressWarnings("checkstyle:MagicNumber")
class StockCacheServiceTemplateIntegrationTest {

    private static final String PRICE = "price_";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String PRICE_UNKNOWN = PRICE + UNKNOWN;
    @Autowired
    private StockCacheServiceTemplate underTest;
    @Autowired
    private StockBatchServiceCaller batchServiceCaller;
    @Autowired
    private StockService stockService;
    @Autowired
    private CacheManager cacheManager;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldNotPutAnythingWhenNoResultsFound()
            throws BatchServiceException {
        //given
        // create the test request
        final SortedSet<String> request = new TreeSet<>(Arrays.asList("A", "B", "C"));

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(Collections.emptyMap(), actual);
        // all items were tried from the cache and missed
        final var cache = cacheManager.getCache(STOCKS);
        final var inOrder = Mockito.inOrder(cache);
        inOrder.verify(cache).get("price_A", BigDecimal.class);
        // pessimistic strategy aborts after first failure
        inOrder.verify(cache, never()).get("price_B", BigDecimal.class);
        inOrder.verify(cache, never()).get("price_C", BigDecimal.class);
        // nothing is put into the cache as all of them are missed
        inOrder.verify(cache, never()).put(anyString(), any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldCacheWhenResultsAreFound()
            throws BatchServiceException {
        //given
        // create the test request
        final SortedSet<String> request = new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.AMD, StockService.EPAM, UNKNOWN));
        final var expected = stockService.lookupNoLimit(request);

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertIterableEquals(Arrays.asList(StockService.AAPL, StockService.AMD, StockService.EPAM), actual.keySet());
        assertEquals(expected, actual);
        // all items were tried from the cache and missed
        final var cache = cacheManager.getCache(STOCKS);
        final var spyService = batchServiceCaller.getStockService();
        final var inOrder = Mockito.inOrder(cache, spyService);
        inOrder.verify(cache).get(PRICE + StockService.AAPL, BigDecimal.class);
        // pessimistic strategy aborts after first failure
        inOrder.verify(cache, never()).get(PRICE + StockService.AMD, BigDecimal.class);
        inOrder.verify(cache, never()).get(PRICE + StockService.EPAM, BigDecimal.class);
        inOrder.verify(cache, never()).get(PRICE_UNKNOWN, BigDecimal.class);
        // verify service calls and that found items are put into the cache
        inOrder.verify(spyService).lookup(new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.EPAM)));
        inOrder.verify(cache).put(eq(PRICE + StockService.AAPL), any());
        inOrder.verify(cache).put(eq(PRICE + StockService.EPAM), any());
        inOrder.verify(spyService).lookup(new TreeSet<>(Arrays.asList(StockService.AMD, UNKNOWN)));
        inOrder.verify(cache).put(eq(PRICE + StockService.AMD), any());
        inOrder.verify(cache, never()).put(eq(PRICE_UNKNOWN), any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldNotUseRealServiceWhenAllFoundInCache()
            throws BatchServiceException {
        //given
        // create the test request
        final SortedSet<String> request = new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.AMD, StockService.EPAM));
        final Map<String, BigDecimal> expected = underTest.callBatchServiceAndPutAllToCache(request);
        // verify that items where just put into cache
        final var cache = cacheManager.getCache(STOCKS);
        final var spyService = batchServiceCaller.getStockService();
        verify(spyService).lookup(new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.EPAM)));
        verify(spyService).lookup(new TreeSet<>(Collections.singletonList(StockService.AMD)));
        reset(spyService);
        verify(cache).put(eq(PRICE + StockService.AAPL), any());
        verify(cache).put(eq(PRICE + StockService.AMD), any());
        verify(cache).put(eq(PRICE + StockService.EPAM), any());
        reset(cache);

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertIterableEquals(Arrays.asList(StockService.AAPL, StockService.AMD, StockService.EPAM), actual.keySet());
        assertEquals(expected, actual);
        // all items were tried from the cache and missed
        final var inOrder = Mockito.inOrder(cache, spyService);
        inOrder.verify(cache).get(PRICE + StockService.AAPL, BigDecimal.class);
        inOrder.verify(cache).get(PRICE + StockService.EPAM, BigDecimal.class);
        inOrder.verify(cache).get(PRICE + StockService.AMD, BigDecimal.class);
        // verify service calls are never made, and nothing else is put into the cache
        inOrder.verify(cache, never()).put(eq(PRICE + StockService.AAPL), any());
        inOrder.verify(cache, never()).put(eq(PRICE + StockService.EPAM), any());
        inOrder.verify(cache, never()).put(eq(PRICE + StockService.AMD), any());
        inOrder.verify(spyService, never()).lookup(new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.EPAM)));
        inOrder.verify(spyService, never()).lookup(new TreeSet<>(Collections.singletonList(StockService.AMD)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCallCacheableBatchServiceShouldUseRealServiceOnlyWhenSomeNotFoundInCache()
            throws BatchServiceException {
        //given
        // create the warm-up request
        final SortedSet<String> warmUpRequest = new TreeSet<>(Collections.singletonList(StockService.AAPL));
        underTest.callBatchServiceAndPutAllToCache(warmUpRequest);
        // verify that items where just put into cache
        final var cache = cacheManager.getCache(STOCKS);
        final var spyService = batchServiceCaller.getStockService();
        verify(spyService).lookup(new TreeSet<>(Collections.singletonList(StockService.AAPL)));
        reset(spyService);
        verify(cache).put(eq(PRICE + StockService.AAPL), any());
        reset(cache);
        // create the test request
        final SortedSet<String> request = new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.AMD, StockService.EPAM));
        final var expected = stockService.lookupNoLimit(request);

        //when
        final var actual = underTest.callCacheableBatchService(request);

        //then
        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertIterableEquals(Arrays.asList(StockService.AAPL, StockService.AMD, StockService.EPAM), actual.keySet());
        assertEquals(expected, actual);
        // all items were tried from the cache and missed
        final var inOrder = Mockito.inOrder(cache, spyService);
        inOrder.verify(cache).get(PRICE + StockService.AAPL, BigDecimal.class);
        inOrder.verify(cache).get(PRICE + StockService.EPAM, BigDecimal.class);
        // pessimistic strategy aborts after first failure
        inOrder.verify(cache, never()).get(PRICE + StockService.AMD, BigDecimal.class);
        inOrder.verify(cache, never()).get(PRICE_UNKNOWN, BigDecimal.class);
        // verify service calls and that found items are put into the cache
        inOrder.verify(spyService).lookup(new TreeSet<>(Arrays.asList(StockService.AAPL, StockService.EPAM)));
        inOrder.verify(cache).put(eq(PRICE + StockService.AAPL), any());
        inOrder.verify(cache).put(eq(PRICE + StockService.EPAM), any());
        inOrder.verify(spyService).lookup(new TreeSet<>(Collections.singletonList(StockService.AMD)));
        inOrder.verify(cache).put(eq(PRICE + StockService.AMD), any());
    }
}
