package com.visualspider.admin;

import java.util.List;

public record RulePreviewPageView(
        Long previewSessionId,
        Long ruleId,
        String ruleName,
        String previewUrl,
        List<PreviewFieldResultView> fieldResults
) {
}

