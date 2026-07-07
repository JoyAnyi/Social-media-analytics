package com.company.socialanalytics.dashboard;

public record SystemHealthView(
        String database,
        String kafka,
        String redis,
        String elasticsearch,
        double cpuUsage,
        long usedMemoryBytes,
        long maxMemoryBytes
) {
}
