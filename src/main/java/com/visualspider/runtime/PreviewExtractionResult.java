package com.visualspider.runtime;

import com.visualspider.persistence.CrawlSelectorCandidate;

public record PreviewExtractionResult(
        boolean success,
        String extractedValue,
        CrawlSelectorCandidate usedCandidate,
        String failureReason
) {
}

