package com.visualspider.admin;

import java.util.List;

public record TaskRunDetailView(
        Long runId,
        String status,
        String sourceUrl,
        Long durationMs,
        String errorMessage,
        String startedAtText,
        String finishedAtText,
        List<TaskSnapshotView> snapshots
) {
}

