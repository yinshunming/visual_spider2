package com.visualspider.admin;

import java.util.List;

public record PreviewFieldResultView(
        Long fieldId,
        String fieldName,
        String fieldType,
        String extractedValue,
        boolean success,
        boolean valid,
        String validationMessage,
        String usedSelectorSummary,
        List<PreviewFieldCandidateView> candidates
) {
}

