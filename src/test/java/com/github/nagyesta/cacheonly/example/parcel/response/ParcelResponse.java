package com.github.nagyesta.cacheonly.example.parcel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cache.annotation.EnableCaching;

@Data
@AllArgsConstructor
@EnableCaching
public class ParcelResponse {
    private String id;
    private ParcelStatus status;
}
