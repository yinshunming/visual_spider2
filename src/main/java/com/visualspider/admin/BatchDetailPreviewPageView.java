package com.visualspider.admin;

import java.util.List;

public record BatchDetailPreviewPageView(
        Long previewSessionId,
        Long listRuleId,
        Long detailRuleId,
        String listRuleName,
        String detailRuleName,
        String sourceUrl,
        List<BatchDetailPreviewItemView> items
) {
}
