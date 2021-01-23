package com.github.nagyesta.cacheonly.example.parcel;

import com.github.nagyesta.cacheonly.core.conurrent.ConcurrentCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.raw.concurrent.AsyncBatchServiceCaller;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.concurrent.AsyncPartialCacheSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParcelCacheServiceTemplate
        extends ConcurrentCacheServiceTemplate<List<String>, List<ParcelResponse>, String, ParcelResponse, String, String> {

    @Autowired
    public ParcelCacheServiceTemplate(
            final @NotNull AsyncPartialCacheSupport<String, ParcelResponse, String, String> partialCacheSupport,
            final @NotNull BatchRequestTransformer<List<String>, String, String> batchRequestTransformer,
            final @NotNull BatchResponseTransformer<List<ParcelResponse>, ParcelResponse, String> batchResponseTransformer,
            final @NotNull AsyncBatchServiceCaller<List<String>, List<ParcelResponse>> batchServiceCaller) {
        super(partialCacheSupport, batchRequestTransformer, batchResponseTransformer, batchServiceCaller);
    }
}
