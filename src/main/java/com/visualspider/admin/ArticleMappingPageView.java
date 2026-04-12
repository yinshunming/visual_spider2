package com.visualspider.admin;

import java.util.List;
import java.util.Map;

public record ArticleMappingPageView(
        Long ruleId,
        String ruleName,
        Long publishedVersionId,
        Integer publishedVersionNo,
        List<ArticleFieldOptionView> fieldOptions,
        Map<String, Long> selectedMappings,
        String lastRunMessage
) {
}

