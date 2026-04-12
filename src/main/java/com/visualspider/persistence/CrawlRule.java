package com.visualspider.persistence;

import java.time.LocalDateTime;

public class CrawlRule {

    private Long id;
    private String ruleName;
    private Long sourcePreviewSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Long getSourcePreviewSessionId() {
        return sourcePreviewSessionId;
    }

    public void setSourcePreviewSessionId(Long sourcePreviewSessionId) {
        this.sourcePreviewSessionId = sourcePreviewSessionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

