package com.visualspider.runtime;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class FieldValidationService {

    public FieldValidationResult validate(String fieldType, String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return new FieldValidationResult(false, normalized, "字段值为空");
        }
        if (normalized.length() > 2000) {
            return new FieldValidationResult(false, normalized, "字段值过长");
        }

        return switch (fieldType) {
            case "URL" -> validateUrl(normalized);
            case "DATETIME" -> validateDateTime(normalized);
            default -> new FieldValidationResult(true, normalized, "校验通过");
        };
    }

    private FieldValidationResult validateUrl(String value) {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return new FieldValidationResult(true, value, "校验通过");
        }
        return new FieldValidationResult(false, value, "URL 格式不正确");
    }

    private FieldValidationResult validateDateTime(String value) {
        try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            return new FieldValidationResult(true, value, "校验通过");
        } catch (DateTimeParseException ignored) {
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime.parse(value, formatter);
            return new FieldValidationResult(true, value, "校验通过");
        } catch (DateTimeParseException ignored) {
        }
        return new FieldValidationResult(false, value, "时间格式不正确");
    }
}

