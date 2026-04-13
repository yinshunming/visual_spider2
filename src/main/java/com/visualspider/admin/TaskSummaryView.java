package com.visualspider.admin;

public record TaskSummaryView(
        Long id,
        String taskName,
        String urlTemplate,
        Long ruleVersionId,
        String cronExpression,
        String status
) {
}

