package com.visualspider.admin;

import java.util.List;

public record RuleFieldSummaryView(
        Long fieldId,
        String fieldName,
        String fieldType,
        String fieldRole,
        String selectedText,
        String domPath,
        List<String> selectorSummaries
) {
}
