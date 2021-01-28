package com.github.nagyesta.cacheonly.example.stock.transform;

import com.github.nagyesta.cacheonly.transform.common.CollectionBasedRequestTransformer;
import org.springframework.stereotype.Component;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StockBatchRequestTransformer extends CollectionBasedRequestTransformer<SortedSet<String>, String, String> {

    public StockBatchRequestTransformer() {
        super(Collectors.toCollection(TreeSet::new), Function.identity());
    }
}
