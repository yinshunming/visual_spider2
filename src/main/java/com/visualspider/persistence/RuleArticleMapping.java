package com.visualspider.persistence;

import java.time.LocalDateTime;

public class RuleArticleMapping {

    private Long id;
    private Long ruleVersionId;
    private Long fieldId;
    private String articleColumn;
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

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getArticleColumn() {
        return articleColumn;
    }

    public void setArticleColumn(String articleColumn) {
        this.articleColumn = articleColumn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

