package com.visualspider.runtime;

public record SelectorCandidateDraft(
        String selectorType,
        String selectorValue,
        int priority
) {
}

