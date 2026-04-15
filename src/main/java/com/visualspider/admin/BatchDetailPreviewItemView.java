package com.visualspider.admin;

import java.util.List;

public record BatchDetailPreviewItemView(
        int itemIndex,
        String titleText,
        String detailUrl,
        String timeText,
        boolean success,
        String errorMessage,
        List<PreviewFieldResultView> fieldResults
) {
}
