package com.visualspider.persistence;

import java.time.LocalDateTime;

public class RulePreviewRun {

    private Long id;
    private Long ruleVersionId;
    private Long previewSessionId;
    private String sourceUrl;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleVersionId() {
        return ruleVersionId;
    }

    public void setRuleVersionId(Long ruleVersionId) {
        this.ruleVersionId = ruleVersionId;
    }

    public Long getPreviewSessionId() {
        return previewSessionId;
    }

    public void setPreviewSessionId(Long previewSessionId) {
        this.previewSessionId = previewSessionId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

