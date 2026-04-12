package com.visualspider.admin;

import java.util.List;

public record RuleVersionPageView(
        Long ruleId,
        String ruleName,
        Long previewSessionId,
        Long draftVersionId,
        Integer draftVersionNo,
        String draftStatus,
        List<RuleVersionSummaryView> versions
) {
}
