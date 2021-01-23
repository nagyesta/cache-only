package com.github.nagyesta.cacheonly.example.stock.transform;

import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class StockBatchResponseTransformer
        implements BatchResponseTransformer<SortedMap<String, BigDecimal>, BigDecimal, String> {

    @NotNull
    @Override
    public Map<String, BigDecimal> splitToPartialResponse(final @NotNull SortedMap<String, BigDecimal> batchResponse) {
        return batchResponse;
    }

    @Nullable
    @Override
    public SortedMap<String, BigDecimal> mergeToBatchResponse(final @NotNull Map<String, BigDecimal> entityMap) {
        return new TreeMap<>(entityMap);
    }
}
