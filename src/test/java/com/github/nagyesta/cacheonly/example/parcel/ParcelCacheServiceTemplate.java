package com.github.nagyesta.cacheonly.example.parcel;

import com.github.nagyesta.cacheonly.core.DefaultCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParcelCacheServiceTemplate
        extends DefaultCacheServiceTemplate<List<String>, List<ParcelResponse>, String, ParcelResponse, String, String> {

    @Autowired
    public ParcelCacheServiceTemplate(
            final PartialCacheSupport<String, ParcelResponse, String, String> partialCacheSupport,
            final BatchRequestTransformer<List<String>, String, String> batchRequestTransformer,
            final BatchResponseTransformer<List<ParcelResponse>, ParcelResponse, String> batchResponseTransformer,
            final BatchServiceCaller<List<String>, List<ParcelResponse>> batchServiceCaller) {
        super(partialCacheSupport, batchRequestTransformer, batchResponseTransformer, batchServiceCaller);
    }
}
