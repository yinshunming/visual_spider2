package com.visualspider.admin;

import java.util.List;

public record RuleDraftPageView(
        Long previewSessionId,
        String previewTitle,
        String previewUrl,
        String screenshotUrl,
        Long ruleId,
        String ruleName,
        List<SelectableElementView> selectableElements,
        List<RuleFieldSummaryView> fields
) {
}

