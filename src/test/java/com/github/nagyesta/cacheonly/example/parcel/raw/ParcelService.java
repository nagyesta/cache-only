package com.github.nagyesta.cacheonly.example.parcel.raw;

import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:DesignForExtension"})
public class ParcelService {

    public List<ParcelResponse> lookup(final List<String> batchRequest) {
        Assert.isTrue(batchRequest.size() <= 10, "Higher limit is 10.");
        return batchRequest.stream()
                .sorted()
                .map(id -> new ParcelResponse(id, lookup(id)))
                .collect(Collectors.toList());
    }

    public ParcelStatus lookup(final String id) {
        ParcelStatus result = ParcelStatus.NEW;
        if (id.startsWith("A")) {
            result = ParcelStatus.PICKED_UP;
        } else if (id.startsWith("B")) {
            result = ParcelStatus.IN_TRANSIT;
        } else if (id.startsWith("C")) {
            result = ParcelStatus.DELIVERY;
        } else if (id.startsWith("D")) {
            result = ParcelStatus.DELIVERED;
        }
        return result;
    }
}
