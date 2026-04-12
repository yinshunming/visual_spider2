package com.visualspider.admin;

public record PreviewFieldCandidateView(
        Long candidateId,
        String selectorType,
        String selectorValue,
        boolean selected
) {
}

