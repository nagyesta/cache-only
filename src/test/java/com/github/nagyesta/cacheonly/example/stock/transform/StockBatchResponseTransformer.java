package com.github.nagyesta.cacheonly.example.stock.transform;

import com.github.nagyesta.cacheonly.transform.common.MapBasedResponseTransformer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class StockBatchResponseTransformer
        extends MapBasedResponseTransformer<SortedMap<String, BigDecimal>, BigDecimal, String> {

    public StockBatchResponseTransformer() {
        super(TreeMap::new);
    }

}
