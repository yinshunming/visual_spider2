package com.visualspider.admin;

import java.util.List;

public record RuleDraftPageView(
        Long previewSessionId,
        String previewTitle,
        String previewUrl,
        String screenshotUrl,
        Long ruleId,
        String ruleName,
        Long versionId,
        Integer versionNo,
        String versionStatus,
        List<SelectableElementView> selectableElements,
        List<RuleFieldSummaryView> fields
) {
}
