package com.visualspider.admin;

import java.util.List;

public record RulePreviewExecutionView(
        Long ruleId,
        String ruleName,
        String sourceUrl,
        List<PreviewFieldResultView> fieldResults
) {
}
