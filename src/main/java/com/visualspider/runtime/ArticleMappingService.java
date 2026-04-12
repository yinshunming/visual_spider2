package com.visualspider.runtime;

import com.visualspider.admin.ArticleFieldOptionView;
import com.visualspider.admin.ArticleMappingForm;
import com.visualspider.admin.ArticleMappingPageView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.RuleArticleMapping;
import com.visualspider.persistence.RuleArticleMappingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleMappingService {

    private final CrawlRuleMapper crawlRuleMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;
    private final CrawlRuleFieldMapper crawlRuleFieldMapper;
    private final RuleArticleMappingMapper ruleArticleMappingMapper;

    public ArticleMappingService(CrawlRuleMapper crawlRuleMapper,
                                 CrawlRuleVersionMapper crawlRuleVersionMapper,
                                 CrawlRuleFieldMapper crawlRuleFieldMapper,
                                 RuleArticleMappingMapper ruleArticleMappingMapper) {
        this.crawlRuleMapper = crawlRuleMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.ruleArticleMappingMapper = ruleArticleMappingMapper;
    }

    public ArticleMappingPageView buildPage(Long ruleId, String runMessage) {
        CrawlRule rule = requireRule(ruleId);
        CrawlRuleVersion published = requirePublishedVersion(ruleId);
        List<CrawlRuleField> fields = crawlRuleFieldMapper.findByRuleVersionId(published.getId());
        Map<String, Long> selectedMappings = new LinkedHashMap<>();
        for (RuleArticleMapping mapping : ruleArticleMappingMapper.findByRuleVersionId(published.getId())) {
            selectedMappings.put(mapping.getArticleColumn(), mapping.getFieldId());
        }

        return new ArticleMappingPageView(
                ruleId,
                rule.getRuleName(),
                published.getId(),
                published.getVersionNo(),
                fields.stream().map(field -> new ArticleFieldOptionView(field.getId(), field.getFieldName(), field.getFieldType())).toList(),
                selectedMappings,
                runMessage
        );
    }

    public ArticleMappingForm buildForm(Long ruleId) {
        CrawlRuleVersion published = requirePublishedVersion(ruleId);
        ArticleMappingForm form = new ArticleMappingForm();
        for (RuleArticleMapping mapping : ruleArticleMappingMapper.findByRuleVersionId(published.getId())) {
            apply(form, mapping.getArticleColumn(), mapping.getFieldId());
        }
        return form;
    }

    @Transactional
    public void saveMappings(Long ruleId, ArticleMappingForm form) {
        CrawlRuleVersion published = requirePublishedVersion(ruleId);
        ruleArticleMappingMapper.deleteByRuleVersionId(published.getId());
        saveIfPresent(published.getId(), "source_url", form.getSourceUrlFieldId());
        saveIfPresent(published.getId(), "title", form.getTitleFieldId());
        saveIfPresent(published.getId(), "author", form.getAuthorFieldId());
        saveIfPresent(published.getId(), "published_at", form.getPublishedAtFieldId());
        saveIfPresent(published.getId(), "summary", form.getSummaryFieldId());
        saveIfPresent(published.getId(), "content", form.getContentFieldId());
        saveIfPresent(published.getId(), "cover_image", form.getCoverImageFieldId());
    }

    private void saveIfPresent(Long versionId, String articleColumn, Long fieldId) {
        if (fieldId == null) {
            return;
        }
        RuleArticleMapping mapping = new RuleArticleMapping();
        mapping.setRuleVersionId(versionId);
        mapping.setFieldId(fieldId);
        mapping.setArticleColumn(articleColumn);
        ruleArticleMappingMapper.insert(mapping);
    }

    private void apply(ArticleMappingForm form, String articleColumn, Long fieldId) {
        switch (articleColumn) {
            case "source_url" -> form.setSourceUrlFieldId(fieldId);
            case "title" -> form.setTitleFieldId(fieldId);
            case "author" -> form.setAuthorFieldId(fieldId);
            case "published_at" -> form.setPublishedAtFieldId(fieldId);
            case "summary" -> form.setSummaryFieldId(fieldId);
            case "content" -> form.setContentFieldId(fieldId);
            case "cover_image" -> form.setCoverImageFieldId(fieldId);
            default -> {
            }
        }
    }

    private CrawlRule requireRule(Long ruleId) {
        CrawlRule rule = crawlRuleMapper.findById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        return rule;
    }

    public CrawlRuleVersion requirePublishedVersion(Long ruleId) {
        CrawlRuleVersion version = crawlRuleVersionMapper.findLatestPublishedByRuleId(ruleId);
        if (version == null) {
            throw new IllegalStateException("规则尚未发布，无法配置 article 映射");
        }
        return version;
    }
}

