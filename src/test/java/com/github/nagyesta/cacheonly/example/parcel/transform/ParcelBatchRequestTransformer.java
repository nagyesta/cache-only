package com.github.nagyesta.cacheonly.example.parcel.transform;

import com.github.nagyesta.cacheonly.transform.common.CollectionBasedRequestTransformer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ParcelBatchRequestTransformer extends CollectionBasedRequestTransformer<List<String>, String, String> {

    public ParcelBatchRequestTransformer() {
        super(Collectors.toList(), Function.identity());
    }
}
