package com.github.nagyesta.cacheonly.example.parcel.transform;

import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.transform.common.CollectionBasedResponseTransformer;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class ParcelBatchResponseTransformer
        extends CollectionBasedResponseTransformer<List<ParcelResponse>, ParcelResponse, String> {

    public ParcelBatchResponseTransformer() {
        super(Collectors.toList(), ParcelResponse::id, true);
    }

    @Override
    public @Nullable List<ParcelResponse> mergeToBatchResponse(
            final Map<String, ParcelResponse> entityMap) {
        return super.mergeToBatchResponse(new TreeMap<>(entityMap));
    }
}
