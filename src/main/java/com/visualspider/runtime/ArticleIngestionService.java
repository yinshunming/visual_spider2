package com.visualspider.runtime;

import com.visualspider.persistence.Article;
import com.visualspider.persistence.ArticleMapper;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.PagePreviewSession;
import com.visualspider.persistence.RuleArticleMapping;
import com.visualspider.persistence.RuleArticleMappingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleIngestionService {

    private final ArticleMappingService articleMappingService;
    private final RuleArticleMappingMapper ruleArticleMappingMapper;
    private final CrawlRuleFieldMapper crawlRuleFieldMapper;
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;
    private final PagePreviewSessionService pagePreviewSessionService;
    private final PlaywrightService playwrightService;
    private final ArticleMapper articleMapper;

    public ArticleIngestionService(ArticleMappingService articleMappingService,
                                   RuleArticleMappingMapper ruleArticleMappingMapper,
                                   CrawlRuleFieldMapper crawlRuleFieldMapper,
                                   CrawlSelectorCandidateMapper crawlSelectorCandidateMapper,
                                   PagePreviewSessionService pagePreviewSessionService,
                                   PlaywrightService playwrightService,
                                   ArticleMapper articleMapper) {
        this.articleMappingService = articleMappingService;
        this.ruleArticleMappingMapper = ruleArticleMappingMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.crawlSelectorCandidateMapper = crawlSelectorCandidateMapper;
        this.pagePreviewSessionService = pagePreviewSessionService;
        this.playwrightService = playwrightService;
        this.articleMapper = articleMapper;
    }

    @Transactional
    public String ingest(Long ruleId) {
        CrawlRuleVersion published = articleMappingService.requirePublishedVersion(ruleId);
        List<RuleArticleMapping> mappings = ruleArticleMappingMapper.findByRuleVersionId(published.getId());
        if (mappings.isEmpty()) {
            throw new IllegalStateException("尚未配置 article 字段映射");
        }

        PagePreviewSession previewSession = pagePreviewSessionService.getSession(published.getSourcePreviewSessionId());
        String sourcePageUrl = previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();

        Map<String, String> values = new HashMap<>();
        for (RuleArticleMapping mapping : mappings) {
            CrawlRuleField field = findField(published.getId(), mapping.getFieldId());
            CrawlSelectorCandidate candidate = selectBestCandidate(field);
            PreviewExtractionResult extraction = playwrightService.extractValue(sourcePageUrl, field.getFieldType(), candidate);
            if (extraction.success()) {
                values.put(mapping.getArticleColumn(), extraction.extractedValue());
            }
        }

        String sourceUrl = values.get("source_url");
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalStateException("source_url 映射结果为空，无法入库");
        }

        Article article = articleMapper.findBySourceUrl(sourceUrl);
        boolean inserted = article == null;
        if (article == null) {
            article = new Article();
        }

        article.setSourceUrl(sourceUrl);
        article.setTitle(values.get("title"));
        article.setAuthor(values.get("author"));
        article.setPublishedAt(parseDateTime(values.get("published_at")));
        article.setSummary(values.get("summary"));
        article.setContent(values.get("content"));
        article.setCoverImage(values.get("cover_image"));

        if (inserted) {
            articleMapper.insert(article);
            return "已插入 article，ID=" + article.getId() + "，source_url=" + article.getSourceUrl();
        }
        articleMapper.update(article);
        return "已更新 article，ID=" + article.getId() + "，source_url=" + article.getSourceUrl();
    }

    private CrawlRuleField findField(Long versionId, Long fieldId) {
        return crawlRuleFieldMapper.findByRuleVersionId(versionId).stream()
                .filter(field -> field.getId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("映射字段不存在"));
    }

    private CrawlSelectorCandidate selectBestCandidate(CrawlRuleField field) {
        List<CrawlSelectorCandidate> candidates = crawlSelectorCandidateMapper.findByFieldId(field.getId());
        if (candidates.isEmpty()) {
            throw new IllegalStateException("字段 " + field.getFieldName() + " 没有 selector 候选");
        }
        if ("URL".equals(field.getFieldType()) || "DATETIME".equals(field.getFieldType())) {
            for (CrawlSelectorCandidate candidate : candidates) {
                if (candidate.getSelectorType() != null && "attribute".equalsIgnoreCase(candidate.getSelectorType().trim())) {
                    return candidate;
                }
            }
        }
        return candidates.getFirst();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}

