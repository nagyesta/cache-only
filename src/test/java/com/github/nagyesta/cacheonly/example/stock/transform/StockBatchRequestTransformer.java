package com.github.nagyesta.cacheonly.example.stock.transform;

import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StockBatchRequestTransformer implements BatchRequestTransformer<SortedSet<String>, String, String> {

    @Override
    public Map<String, String> splitToPartialRequest(final SortedSet<String> batchRequest) {
        return batchRequest.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    @Override
    public SortedSet<String> mergeToBatchRequest(final Map<String, String> requestMap) {
        return new TreeSet<>(requestMap.values());
    }
}
