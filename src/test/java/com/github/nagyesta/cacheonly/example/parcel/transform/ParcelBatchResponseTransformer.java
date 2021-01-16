package com.github.nagyesta.cacheonly.example.parcel.transform;

import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ParcelBatchResponseTransformer
        implements BatchResponseTransformer<List<ParcelResponse>, ParcelResponse, String> {

    @Override
    public Map<String, ParcelResponse> splitToPartialResponse(final List<ParcelResponse> batchResponse) {
        return batchResponse.stream()
                .collect(Collectors.toMap(ParcelResponse::getId, Function.identity()));
    }

    @Override
    public List<ParcelResponse> mergeToBatchResponse(final Map<String, ParcelResponse> entityMap) {
        return entityMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
