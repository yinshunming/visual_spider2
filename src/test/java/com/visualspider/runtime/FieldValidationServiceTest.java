package com.visualspider.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldValidationServiceTest {

    private final FieldValidationService service = new FieldValidationService();

    @Test
    void shouldValidateUrlField() {
        FieldValidationResult result = service.validate("URL", "https://www.sina.com.cn");
        assertTrue(result.valid());
    }

    @Test
    void shouldRejectInvalidUrlField() {
        FieldValidationResult result = service.validate("URL", "www.sina.com.cn");
        assertFalse(result.valid());
    }

    @Test
    void shouldRejectInvalidDateTimeField() {
        FieldValidationResult result = service.validate("DATETIME", "not-a-date");
        assertFalse(result.valid());
    }
}
