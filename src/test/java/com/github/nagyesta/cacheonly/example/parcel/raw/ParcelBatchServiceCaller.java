package com.github.nagyesta.cacheonly.example.parcel.raw;

import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.raw.concurrent.AsyncBatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.mockito.Mockito.spy;

@Service
public class ParcelBatchServiceCaller
        implements AsyncBatchServiceCaller<List<String>, List<ParcelResponse>> {

    private static final int PARTITION_SIZE = 10;

    private final ParcelService parcelService;

    @Autowired
    public ParcelBatchServiceCaller(final ParcelService parcelService) {
        this.parcelService = spy(parcelService);
    }

    @Override
    public int maxPartitionSize() {
        return PARTITION_SIZE;
    }

    @NotNull
    @Override
    public CacheRefreshStrategy refreshStrategy() {
        return CacheRefreshStrategy.NEVER_CACHE;
    }

    @Nullable
    @Override
    @SuppressWarnings("RedundantThrows")
    public List<ParcelResponse> callBatchService(final @NotNull List<String> batchRequest)
            throws BatchServiceException {
        // we call the service here
        return parcelService.lookup(batchRequest);
    }

    @NotNull
    public final ParcelService getParcelService() {
        return parcelService;
    }
}
