package com.visualspider.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RuleDraftFieldForm {

    @NotNull
    private Long previewSessionId;
    private Long ruleId;

    private String ruleName;

    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    @NotBlank(message = "字段类型不能为空")
    private String fieldType = "TEXT";

    @NotBlank(message = "请先选择页面元素")
    private String selectedTagName;

    @NotBlank(message = "请先选择页面元素")
    private String selectedText;

    @NotBlank(message = "请先选择页面元素")
    private String domPath;

    private String elementIdValue;
    private String classNames;
    private String hrefValue;
    private String titleValue;
    private String dateTimeValue;

    public Long getPreviewSessionId() {
        return previewSessionId;
    }

    public void setPreviewSessionId(Long previewSessionId) {
        this.previewSessionId = previewSessionId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
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

    public String getElementIdValue() {
        return elementIdValue;
    }

    public void setElementIdValue(String elementIdValue) {
        this.elementIdValue = elementIdValue;
    }

    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(String classNames) {
        this.classNames = classNames;
    }

    public String getHrefValue() {
        return hrefValue;
    }

    public void setHrefValue(String hrefValue) {
        this.hrefValue = hrefValue;
    }

    public String getTitleValue() {
        return titleValue;
    }

    public void setTitleValue(String titleValue) {
        this.titleValue = titleValue;
    }

    public String getDateTimeValue() {
        return dateTimeValue;
    }

    public void setDateTimeValue(String dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }
}
