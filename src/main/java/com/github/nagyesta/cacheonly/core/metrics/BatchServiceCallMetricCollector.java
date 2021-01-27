package com.github.nagyesta.cacheonly.core.metrics;

/**
 * Provides a way for metrics collection.
 */
public interface BatchServiceCallMetricCollector {

    /**
     * Called when cache GET is performed.
     *
     * @param gets The amount we need to register.
     */
    void cacheGet(int gets);

    /**
     * Called when cache HIT happens.
     *
     * @param hits The amount we need to register.
     */
    void cacheHit(int hits);

    /**
     * Called when cache MISS happens.
     *
     * @param miss The amount we need to register.
     */
    void cacheMiss(int miss);

    /**
     * Called when cache PUT is performed.
     *
     * @param puts The amount we need to register.
     */
    void cachePut(int puts);

    /**
     * Called when partitions are created for calling origin service.
     *
     * @param partitions The number of partitions.
     */
    void partitionsCreated(int partitions);

    /**
     * Called when calling origin service failed for partitions.
     *
     * @param partitions The number of partitions.
     */
    void partitionsFailed(int partitions);

    /**
     * Called when calling origin service succeeded for partitions.
     *
     * @param partitions The number of partitions.
     */
    void partitionsSucceeded(int partitions);
}
