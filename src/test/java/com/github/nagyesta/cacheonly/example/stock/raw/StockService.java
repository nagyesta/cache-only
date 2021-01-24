package com.github.nagyesta.cacheonly.example.stock.raw;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:JavadocVariable", "checkstyle:DesignForExtension"})
public class StockService {

    public static final String AAPL = "AAPL";
    public static final String AMD = "AMD";
    public static final String EPAM = "EPAM";
    public static final String GOOGL = "GOOGL";
    public static final String INTC = "INTC";
    public static final String NVDA = "NVDA";
    private final Map<String, BigDecimal> priceMap;

    public StockService() {
        final Map<String, BigDecimal> map = new TreeMap<>();
        map.put(AAPL, new BigDecimal("127.14"));
        map.put(AMD, new BigDecimal("88.21"));
        map.put(EPAM, new BigDecimal("348.01"));
        map.put(GOOGL, new BigDecimal("1727.62"));
        map.put(INTC, new BigDecimal("57.58"));
        map.put(NVDA, new BigDecimal("514.38"));
        priceMap = map;
    }

    @NotNull
    public Map<String, BigDecimal> lookup(final @NotNull Set<String> stockNames) {
        Assert.isTrue(stockNames.size() < 3, "Only 2 at a time please...");
        return lookupNoLimit(stockNames);
    }

    @NotNull
    public Map<String, BigDecimal> lookupNoLimit(final @NotNull Set<String> stockNames) {
        return priceMap.entrySet().stream()
                .filter(e -> stockNames.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
