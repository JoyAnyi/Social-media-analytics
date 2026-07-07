package com.company.socialanalytics.feed;

public enum SimulationSpeed {
    SLOW(1),
    MEDIUM(3),
    FAST(8);

    private final int defaultBatchSize;

    SimulationSpeed(int defaultBatchSize) {
        this.defaultBatchSize = defaultBatchSize;
    }

    int defaultBatchSize() {
        return defaultBatchSize;
    }
}
