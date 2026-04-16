package com.visualspider.admin;

import java.util.List;

public record TaskRunStatsView(
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
