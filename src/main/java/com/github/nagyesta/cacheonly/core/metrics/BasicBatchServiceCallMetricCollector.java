package com.github.nagyesta.cacheonly.core.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Basic implementation of {@link BatchServiceCallMetricCollector}.
 */
public final class BasicBatchServiceCallMetricCollector implements BatchServiceCallMetricCollector {

    private final AtomicLong cacheGet = new AtomicLong(0L);
    private final AtomicLong cacheHit = new AtomicLong(0L);
    private final AtomicLong cacheMiss = new AtomicLong(0L);
    private final AtomicLong cachePut = new AtomicLong(0L);
    private final AtomicLong partitionCreated = new AtomicLong(0L);
    private final AtomicLong partitionFailed = new AtomicLong(0L);
    private final AtomicLong partitionSucceeded = new AtomicLong(0L);

    @Override
    public void cacheGet(final int gets) {
        cacheGet.addAndGet(gets);
    }

    @Override
    public void cacheHit(final int hits) {
        cacheHit.addAndGet(hits);
    }

    @Override
    public void cacheMiss(final int miss) {
        cacheMiss.addAndGet(miss);
    }

    @Override
    public void cachePut(final int puts) {
        cachePut.addAndGet(puts);
    }

    @Override
    public void partitionsCreated(final int partitions) {
        partitionCreated.addAndGet(partitions);
    }

    @Override
    public void partitionsFailed(final int partitions) {
        partitionFailed.addAndGet(partitions);
    }

    @Override
    public void partitionsSucceeded(final int partitions) {
        partitionSucceeded.addAndGet(partitions);
    }

    public long getCacheGet() {
        return cacheGet.get();
    }

    public long getCacheHit() {
        return cacheHit.get();
    }

    public long getCacheMiss() {
        return cacheMiss.get();
    }

    public long getCachePut() {
        return cachePut.get();
    }

    public long getPartitionCreated() {
        return partitionCreated.get();
    }

    public long getPartitionFailed() {
        return partitionFailed.get();
    }

    public long getPartitionSucceeded() {
        return partitionSucceeded.get();
    }
}
