package com.visualspider.admin;

import java.util.List;

public record ListDiscoveryPageView(
        Long previewSessionId,
        Long ruleId,
        String ruleName,
        String sourceUrl,
        List<ListDiscoveryItemView> items
) {
}

