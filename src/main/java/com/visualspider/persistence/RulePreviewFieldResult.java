package com.visualspider.persistence;

import java.time.LocalDateTime;

public class RulePreviewFieldResult {

    private Long id;
    private Long previewRunId;
    private Long fieldId;
    private Long selectorCandidateId;
    private String extractedValue;
    private String status;
    private String validationMessage;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPreviewRunId() {
        return previewRunId;
    }

    public void setPreviewRunId(Long previewRunId) {
        this.previewRunId = previewRunId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getSelectorCandidateId() {
        return selectorCandidateId;
    }

    public void setSelectorCandidateId(Long selectorCandidateId) {
        this.selectorCandidateId = selectorCandidateId;
    }

    public String getExtractedValue() {
        return extractedValue;
    }

    public void setExtractedValue(String extractedValue) {
        this.extractedValue = extractedValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

