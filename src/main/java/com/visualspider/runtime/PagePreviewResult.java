package com.visualspider.runtime;

public record PagePreviewResult(
        String requestedUrl,
        String finalUrl,
        String title,
        long durationMs,
        String screenshotPath
) {
}

