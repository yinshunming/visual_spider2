package com.visualspider.runtime;

import java.util.List;

public record CrawlExecutionSummary(
        String mode,
        int listDiscoveryCount,
        int detailCount,
        int articleInsertedCount,
        int articleUpdatedCount,
        int articleSkippedCount,
        int failureCount,
        List<String> failureReasons
) {
}
