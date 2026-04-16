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
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ArticleUrlNormalizer articleUrlNormalizer;
    private final SinaDateTimeParser sinaDateTimeParser;

    public ArticleIngestionService(ArticleMappingService articleMappingService,
                                   RuleArticleMappingMapper ruleArticleMappingMapper,
                                   CrawlRuleFieldMapper crawlRuleFieldMapper,
                                   CrawlSelectorCandidateMapper crawlSelectorCandidateMapper,
                                   PagePreviewSessionService pagePreviewSessionService,
                                   PlaywrightService playwrightService,
                                   ArticleMapper articleMapper) {
        this(articleMappingService,
                ruleArticleMappingMapper,
                crawlRuleFieldMapper,
                crawlSelectorCandidateMapper,
                pagePreviewSessionService,
                playwrightService,
                articleMapper,
                new ArticleUrlNormalizer(),
                new SinaDateTimeParser());
    }

    @Autowired
    public ArticleIngestionService(ArticleMappingService articleMappingService,
                                   RuleArticleMappingMapper ruleArticleMappingMapper,
                                   CrawlRuleFieldMapper crawlRuleFieldMapper,
                                   CrawlSelectorCandidateMapper crawlSelectorCandidateMapper,
                                   PagePreviewSessionService pagePreviewSessionService,
                                   PlaywrightService playwrightService,
                                   ArticleMapper articleMapper,
                                   ArticleUrlNormalizer articleUrlNormalizer,
                                   SinaDateTimeParser sinaDateTimeParser) {
        this.articleMappingService = articleMappingService;
        this.ruleArticleMappingMapper = ruleArticleMappingMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.crawlSelectorCandidateMapper = crawlSelectorCandidateMapper;
        this.pagePreviewSessionService = pagePreviewSessionService;
        this.playwrightService = playwrightService;
        this.articleMapper = articleMapper;
        this.articleUrlNormalizer = articleUrlNormalizer;
        this.sinaDateTimeParser = sinaDateTimeParser;
    }

    @Transactional
    public String ingest(Long ruleId) {
        CrawlRuleVersion published = articleMappingService.requirePublishedVersion(ruleId);
        return ingestArticleByPublishedVersion(published.getId(), resolveSourceUrl(published)).message();
    }

    @Transactional
    public String ingestByPublishedVersion(Long ruleVersionId, String sourcePageUrl) {
        CrawlRuleVersion published = findPublishedVersion(ruleVersionId);
        List<RuleArticleMapping> mappings = ruleArticleMappingMapper.findByRuleVersionId(published.getId());
        if (mappings.isEmpty()) {
            throw new IllegalStateException("尚未配置 article 字段映射");
        }

        if (sourcePageUrl == null || sourcePageUrl.isBlank()) {
            PagePreviewSession previewSession = pagePreviewSessionService.getSession(published.getSourcePreviewSessionId());
            sourcePageUrl = previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();
        }

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

    @Transactional
    public ArticleIngestionResult ingestArticleByPublishedVersion(Long ruleVersionId, String sourcePageUrl) {
        CrawlRuleVersion published = findPublishedVersion(ruleVersionId);
        List<RuleArticleMapping> mappings = ruleArticleMappingMapper.findByRuleVersionId(published.getId());
        if (mappings.isEmpty()) {
            throw new IllegalStateException("Article mapping is not configured");
        }

        if (sourcePageUrl == null || sourcePageUrl.isBlank()) {
            PagePreviewSession previewSession = pagePreviewSessionService.getSession(published.getSourcePreviewSessionId());
            sourcePageUrl = previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();
        }

        Map<String, String> values = new HashMap<>();
        for (RuleArticleMapping mapping : mappings) {
            CrawlRuleField field = findField(published.getId(), mapping.getFieldId());
            CrawlSelectorCandidate candidate = selectBestCandidate(field);
            PreviewExtractionResult extraction = playwrightService.extractValue(sourcePageUrl, field.getFieldType(), candidate);
            if (extraction.success()) {
                values.put(mapping.getArticleColumn(), extraction.extractedValue());
            }
        }

        String sourceUrl = articleUrlNormalizer.normalize(
                values.get("source_url"),
                playwrightService.extractCanonicalUrl(sourcePageUrl),
                sourcePageUrl
        );
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalStateException("source_url extraction is empty");
        }

        Article article = articleMapper.findBySourceUrl(sourceUrl);
        boolean inserted = article == null;
        if (article == null) {
            article = new Article();
        }

        String previousTitle = article.getTitle();
        String previousAuthor = article.getAuthor();
        LocalDateTime previousPublishedAt = article.getPublishedAt();
        String previousSummary = article.getSummary();
        String previousContent = article.getContent();
        String previousCoverImage = article.getCoverImage();

        article.setSourceUrl(sourceUrl);
        article.setTitle(values.get("title"));
        article.setAuthor(values.get("author"));
        article.setPublishedAt(sinaDateTimeParser.parse(values.get("published_at")));
        article.setSummary(values.get("summary"));
        article.setContent(values.get("content"));
        article.setCoverImage(values.get("cover_image"));

        if (inserted) {
            articleMapper.insert(article);
            return ArticleIngestionResult.inserted(article.getSourceUrl(), "Inserted article: " + article.getSourceUrl());
        }
        if (sameValue(previousTitle, article.getTitle())
                && sameValue(previousAuthor, article.getAuthor())
                && sameValue(previousPublishedAt, article.getPublishedAt())
                && sameValue(previousSummary, article.getSummary())
                && sameValue(previousContent, article.getContent())
                && sameValue(previousCoverImage, article.getCoverImage())) {
            return ArticleIngestionResult.skipped(article.getSourceUrl(), "Skipped unchanged article: " + article.getSourceUrl());
        }

        articleMapper.update(article);
        return ArticleIngestionResult.updated(article.getSourceUrl(), "Updated article: " + article.getSourceUrl());
    }

    private String resolveSourceUrl(CrawlRuleVersion published) {
        List<RuleArticleMapping> mappings = ruleArticleMappingMapper.findByRuleVersionId(published.getId());
        PagePreviewSession previewSession = pagePreviewSessionService.getSession(published.getSourcePreviewSessionId());
        return previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();
    }

    private CrawlRuleVersion findPublishedVersion(Long ruleVersionId) {
        CrawlRuleVersion published = articleMappingService.requirePublishedVersionByVersionId(ruleVersionId);
        if (published == null) {
            throw new IllegalStateException("任务绑定的规则版本不是已发布版本");
        }
        return published;
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

    private boolean sameValue(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }
}
