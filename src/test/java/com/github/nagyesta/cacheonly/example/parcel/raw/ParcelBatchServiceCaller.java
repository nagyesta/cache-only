package com.github.nagyesta.cacheonly.example.parcel.raw;

import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.mockito.Mockito.spy;

@Service
public class ParcelBatchServiceCaller
        implements BatchServiceCaller<List<String>, List<ParcelResponse>> {

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

    @Override
    public CacheRefreshStrategy refreshStrategy() {
        return CacheRefreshStrategy.NEVER_CACHE;
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public List<ParcelResponse> callBatchService(final List<String> batchRequest)
            throws BatchServiceException {
        // we call the service here
        return parcelService.lookup(batchRequest);
    }

    public final ParcelService getParcelService() {
        return parcelService;
    }
}
