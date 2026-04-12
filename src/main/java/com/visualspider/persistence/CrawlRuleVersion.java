package com.visualspider.persistence;

import java.time.LocalDateTime;

public class CrawlRuleVersion {

    private Long id;
    private Long ruleId;
    private Integer versionNo;
    private String status;
    private Long sourcePreviewSessionId;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}

