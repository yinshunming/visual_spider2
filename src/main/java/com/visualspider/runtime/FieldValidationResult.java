package com.visualspider.runtime;

public record FieldValidationResult(
        boolean valid,
        String normalizedValue,
        String message
) {
}

