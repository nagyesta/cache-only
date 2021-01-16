package com.github.nagyesta.cacheonly.example.stock.raw;

import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import static org.mockito.Mockito.spy;

@Service
public class StockBatchServiceCaller
        implements BatchServiceCaller<SortedSet<String>, SortedMap<String, BigDecimal>> {

    private static final int PARTITION_SIZE = 2;

    private final StockService stockService;

    @Autowired
    public StockBatchServiceCaller(final StockService stockService) {
        this.stockService = spy(stockService);
    }

    @Override
    public int maxPartitionSize() {
        return PARTITION_SIZE;
    }

    @Override
    public CacheRefreshStrategy refreshStrategy() {
        return CacheRefreshStrategy.PESSIMISTIC;
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public SortedMap<String, BigDecimal> callBatchService(final SortedSet<String> batchRequest)
            throws BatchServiceException {
        // we call the service here
        return new TreeMap<>(stockService.lookup(batchRequest));
    }

    public final StockService getStockService() {
        return stockService;
    }
}
