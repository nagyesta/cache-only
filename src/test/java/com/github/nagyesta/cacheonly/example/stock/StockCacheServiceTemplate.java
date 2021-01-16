package com.github.nagyesta.cacheonly.example.stock;

import com.github.nagyesta.cacheonly.core.DefaultCacheServiceTemplate;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.SortedSet;

@Service
public class StockCacheServiceTemplate
        extends DefaultCacheServiceTemplate<SortedSet<String>, SortedMap<String, BigDecimal>, String, BigDecimal, String, String> {

    @Autowired
    public StockCacheServiceTemplate(
            final PartialCacheSupport<String, BigDecimal, String, String> partialCacheSupport,
            final BatchRequestTransformer<SortedSet<String>, String, String> batchRequestTransformer,
            final BatchResponseTransformer<SortedMap<String, BigDecimal>, BigDecimal, String> batchResponseTransformer,
            final BatchServiceCaller<SortedSet<String>, SortedMap<String, BigDecimal>> batchServiceCaller) {
        super(partialCacheSupport, batchRequestTransformer, batchResponseTransformer, batchServiceCaller);
    }
}
