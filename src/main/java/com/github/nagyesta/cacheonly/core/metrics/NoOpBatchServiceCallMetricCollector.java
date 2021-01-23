package com.github.nagyesta.cacheonly.core.metrics;

/**
 * No-Op implementation of {@link BatchServiceCallMetricCollector}.
 */
public class NoOpBatchServiceCallMetricCollector implements BatchServiceCallMetricCollector {

    @Override
    public void cacheGet(final int gets) {
        //no-op
    }

    @Override
    public void cacheHit(final int hits) {
        //no-op
    }

    @Override
    public void cacheMiss(final int miss) {
        //no-op
    }

    @Override
    public void cachePut(final int puts) {
        //no-op
    }

    @Override
    public void partitionsCreated(final int partitions) {
        //no-op
    }

    @Override
    public void partitionsFailed(final int partitions) {
        //no-op
    }

    @Override
    public void partitionsSucceeded(final int partitions) {
        //no-op
    }
}
