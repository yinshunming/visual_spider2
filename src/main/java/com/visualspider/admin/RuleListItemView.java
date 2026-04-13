package com.visualspider.admin;

public record RuleListItemView(
        Long ruleId,
        String ruleName,
        Long previewSessionId,
        Long draftVersionId,
        Integer draftVersionNo,
        String draftStatus
) {
}

