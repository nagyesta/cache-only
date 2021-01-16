package com.github.nagyesta.cacheonly.example.stock.transform;

import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class StockBatchResponseTransformer
        implements BatchResponseTransformer<SortedMap<String, BigDecimal>, BigDecimal, String> {

    @Override
    public Map<String, BigDecimal> splitToPartialResponse(final SortedMap<String, BigDecimal> batchResponse) {
        return batchResponse;
    }

    @Override
    public SortedMap<String, BigDecimal> mergeToBatchResponse(final Map<String, BigDecimal> entityMap) {
        return new TreeMap<>(entityMap);
    }
}
