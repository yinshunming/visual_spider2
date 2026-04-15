package com.visualspider.persistence;

import java.time.LocalDateTime;

public class CrawlRuleField {

    private Long id;
    private Long ruleVersionId;
    private String fieldName;
    private String fieldType;
    private String fieldRole;
    private String selectedTagName;
    private String selectedText;
    private String domPath;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldRole() {
        return fieldRole;
    }

    public void setFieldRole(String fieldRole) {
        this.fieldRole = fieldRole;
    }

    public String getSelectedTagName() {
        return selectedTagName;
    }

    public void setSelectedTagName(String selectedTagName) {
        this.selectedTagName = selectedTagName;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }

    public String getDomPath() {
        return domPath;
    }

    public void setDomPath(String domPath) {
        this.domPath = domPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
