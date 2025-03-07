package com.github.nagyesta.cacheonly.example.unstable;

import com.github.nagyesta.cacheonly.core.conurrent.ConcurrentCacheServiceTemplate;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UnstableCacheServiceTemplate extends ConcurrentCacheServiceTemplate<List<Long>, List<String>, Long, String, String, Long> {

    private static final BatchRequestTransformer<List<Long>, Long, Long> BATCH_REQUEST_TRANSFORMER =
            new BatchRequestTransformer<>() {
                @NotNull
                @Override
                public Map<Long, Long> splitToPartialRequest(final @NotNull List<Long> batchRequest) {
                    return batchRequest.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
                }

                @Nullable
                @Override
                public List<Long> mergeToBatchRequest(final @NotNull Map<Long, Long> requestMap) {
                    if (requestMap.isEmpty()) {
                        return null;
                    }
                    return new ArrayList<>(requestMap.values());
                }
            };
    private static final BatchResponseTransformer<List<String>, String, Long> BATCH_RESPONSE_TRANSFORMER =
            new BatchResponseTransformer<>() {
                @NotNull
                @Override
                public Map<Long, String> splitToPartialResponse(final @NotNull List<String> batchResponse) {
                    return batchResponse.stream().collect(Collectors.toMap(Long::parseLong, Function.identity()));
                }

                @Nullable
                @Override
                public List<String> mergeToBatchResponse(final @NotNull Map<Long, String> entityMap) {
                    if (entityMap.isEmpty()) {
                        return null;
                    }
                    return new ArrayList<>(entityMap.values());
                }
            };

    public UnstableCacheServiceTemplate() {
        super(new UnstablePartialCacheSupport(), BATCH_REQUEST_TRANSFORMER, BATCH_RESPONSE_TRANSFORMER,
                new UnstableConcurrentBatchServiceCaller());
    }
}
