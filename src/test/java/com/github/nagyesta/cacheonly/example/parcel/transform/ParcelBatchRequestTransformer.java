package com.github.nagyesta.cacheonly.example.parcel.transform;

import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ParcelBatchRequestTransformer implements BatchRequestTransformer<List<String>, String, String> {

    @NotNull
    @Override
    public Map<String, String> splitToPartialRequest(final @NotNull List<String> batchRequest) {
        return batchRequest.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    @Nullable
    @Override
    public List<String> mergeToBatchRequest(final @NotNull Map<String, String> requestMap) {
        return new ArrayList<>(requestMap.values());
    }
}
