package com.visualspider.admin;

public record PreviewSessionView(
        Long id,
        String requestedUrl,
        String finalUrl,
        String pageTitle,
        Long loadDurationMs,
        String status,
        String errorMessage,
        String screenshotUrl
) {
}

