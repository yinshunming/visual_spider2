package com.visualspider.runtime;

public record PageTitleProbeResult(String requestedUrl, String finalUrl, String title, long durationMs) {
}

