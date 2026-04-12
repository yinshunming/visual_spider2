package com.visualspider.admin;

public record RuleVersionSummaryView(
        Long versionId,
        Integer versionNo,
        String status,
        String createdAtText,
        String publishedAtText
) {
}

