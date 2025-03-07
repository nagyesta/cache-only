package com.github.nagyesta.cacheonly.example.parcel.transform;

import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.transform.NoOpPartialCacheSupport;
import org.springframework.stereotype.Component;

@Component
public class ParcelPartialCacheSupport
        extends NoOpPartialCacheSupport<String, ParcelResponse, String, String> {

}
