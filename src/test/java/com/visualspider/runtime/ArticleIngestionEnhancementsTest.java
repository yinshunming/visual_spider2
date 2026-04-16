package com.visualspider.runtime;

import com.visualspider.persistence.Article;
import com.visualspider.persistence.ArticleMapper;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.RuleArticleMapping;
import com.visualspider.persistence.RuleArticleMappingMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class ArticleIngestionEnhancementsTest {

    private final ArticleMappingService articleMappingService = mock(ArticleMappingService.class);
    private final RuleArticleMappingMapper ruleArticleMappingMapper = mock(RuleArticleMappingMapper.class);
    private final CrawlRuleFieldMapper crawlRuleFieldMapper = mock(CrawlRuleFieldMapper.class);
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper = mock(CrawlSelectorCandidateMapper.class);
    private final PagePreviewSessionService pagePreviewSessionService = mock(PagePreviewSessionService.class);
    private final PlaywrightService playwrightService = mock(PlaywrightService.class);
    private final ArticleMapper articleMapper = mock(ArticleMapper.class);

    private final ArticleIngestionService service = new ArticleIngestionService(
            articleMappingService,
            ruleArticleMappingMapper,
            crawlRuleFieldMapper,
            crawlSelectorCandidateMapper,
            pagePreviewSessionService,
            playwrightService,
            articleMapper,
            new ArticleUrlNormalizer(),
            new SinaDateTimeParser()
    );

    @Test
    void shouldNormalizeSourceUrlBeforeDedupingExistingArticle() {
        CrawlRuleVersion published = new CrawlRuleVersion();
        published.setId(1L);
        published.setStatus("PUBLISHED");

        RuleArticleMapping sourceUrlMapping = new RuleArticleMapping();
        sourceUrlMapping.setRuleVersionId(1L);
        sourceUrlMapping.setFieldId(2L);
        sourceUrlMapping.setArticleColumn("source_url");

        RuleArticleMapping titleMapping = new RuleArticleMapping();
        titleMapping.setRuleVersionId(1L);
        titleMapping.setFieldId(4L);
        titleMapping.setArticleColumn("title");

        CrawlRuleField sourceUrlField = new CrawlRuleField();
        sourceUrlField.setId(2L);
        sourceUrlField.setFieldName("urlField");
        sourceUrlField.setFieldType("URL");

        CrawlRuleField titleField = new CrawlRuleField();
        titleField.setId(4L);
        titleField.setFieldName("titleField");
        titleField.setFieldType("TEXT");

        CrawlSelectorCandidate urlCandidate = new CrawlSelectorCandidate();
        urlCandidate.setId(3L);
        urlCandidate.setSelectorType("attribute");
        urlCandidate.setSelectorValue("a.doc-link");

        CrawlSelectorCandidate titleCandidate = new CrawlSelectorCandidate();
        titleCandidate.setId(5L);
        titleCandidate.setSelectorType("css");
        titleCandidate.setSelectorValue("h1");

        Article existing = new Article();
        existing.setId(88L);
        existing.setSourceUrl("https://sports.sina.com.cn/nba/doc-1.shtml");
        existing.setTitle("old title");

        given(articleMappingService.requirePublishedVersionByVersionId(1L)).willReturn(published);
        given(ruleArticleMappingMapper.findByRuleVersionId(1L)).willReturn(List.of(sourceUrlMapping, titleMapping));
        given(crawlRuleFieldMapper.findByRuleVersionId(1L)).willReturn(List.of(sourceUrlField, titleField));
        given(crawlSelectorCandidateMapper.findByFieldId(2L)).willReturn(List.of(urlCandidate));
        given(crawlSelectorCandidateMapper.findByFieldId(4L)).willReturn(List.of(titleCandidate));
        given(playwrightService.extractValue("https://sports.sina.com.cn/nba/doc-1.shtml?from=wap", "URL", urlCandidate))
                .willReturn(new PreviewExtractionResult(true, "https://sports.sina.com.cn/nba/doc-1.shtml?from=wap&vt=4", urlCandidate, null));
        given(playwrightService.extractValue("https://sports.sina.com.cn/nba/doc-1.shtml?from=wap", "TEXT", titleCandidate))
                .willReturn(new PreviewExtractionResult(true, "new title", titleCandidate, null));
        given(playwrightService.extractCanonicalUrl("https://sports.sina.com.cn/nba/doc-1.shtml?from=wap"))
                .willReturn("https://sports.sina.com.cn/nba/doc-1.shtml");
        given(articleMapper.findBySourceUrl("https://sports.sina.com.cn/nba/doc-1.shtml")).willReturn(existing);

        ArticleIngestionResult result = service.ingestArticleByPublishedVersion(1L, "https://sports.sina.com.cn/nba/doc-1.shtml?from=wap");

        then(articleMapper).should().update(any(Article.class));
        assertEquals(ArticleIngestionAction.UPDATED, result.action());
        assertEquals("https://sports.sina.com.cn/nba/doc-1.shtml", result.sourceUrl());
    }

    @Test
    void shouldParseChinesePublishedAtWhenIngestingArticle() {
        CrawlRuleVersion published = new CrawlRuleVersion();
        published.setId(1L);
        published.setStatus("PUBLISHED");

        RuleArticleMapping sourceUrlMapping = new RuleArticleMapping();
        sourceUrlMapping.setRuleVersionId(1L);
        sourceUrlMapping.setFieldId(2L);
        sourceUrlMapping.setArticleColumn("source_url");

        RuleArticleMapping timeMapping = new RuleArticleMapping();
        timeMapping.setRuleVersionId(1L);
        timeMapping.setFieldId(6L);
        timeMapping.setArticleColumn("published_at");

        CrawlRuleField sourceUrlField = new CrawlRuleField();
        sourceUrlField.setId(2L);
        sourceUrlField.setFieldName("urlField");
        sourceUrlField.setFieldType("URL");

        CrawlRuleField timeField = new CrawlRuleField();
        timeField.setId(6L);
        timeField.setFieldName("publishedAt");
        timeField.setFieldType("DATETIME");

        CrawlSelectorCandidate urlCandidate = new CrawlSelectorCandidate();
        urlCandidate.setId(3L);
        urlCandidate.setSelectorType("attribute");
        urlCandidate.setSelectorValue("a.doc-link");

        CrawlSelectorCandidate timeCandidate = new CrawlSelectorCandidate();
        timeCandidate.setId(7L);
        timeCandidate.setSelectorType("attribute");
        timeCandidate.setSelectorValue("time");

        given(articleMappingService.requirePublishedVersionByVersionId(1L)).willReturn(published);
        given(ruleArticleMappingMapper.findByRuleVersionId(1L)).willReturn(List.of(sourceUrlMapping, timeMapping));
        given(crawlRuleFieldMapper.findByRuleVersionId(1L)).willReturn(List.of(sourceUrlField, timeField));
        given(crawlSelectorCandidateMapper.findByFieldId(2L)).willReturn(List.of(urlCandidate));
        given(crawlSelectorCandidateMapper.findByFieldId(6L)).willReturn(List.of(timeCandidate));
        given(playwrightService.extractValue("https://sports.sina.com.cn/nba/doc-1.shtml", "URL", urlCandidate))
                .willReturn(new PreviewExtractionResult(true, "https://sports.sina.com.cn/nba/doc-1.shtml", urlCandidate, null));
        given(playwrightService.extractValue("https://sports.sina.com.cn/nba/doc-1.shtml", "DATETIME", timeCandidate))
                .willReturn(new PreviewExtractionResult(true, "2026年04月16日 09:30", timeCandidate, null));
        given(playwrightService.extractCanonicalUrl("https://sports.sina.com.cn/nba/doc-1.shtml"))
                .willReturn("https://sports.sina.com.cn/nba/doc-1.shtml");
        given(articleMapper.findBySourceUrl("https://sports.sina.com.cn/nba/doc-1.shtml")).willReturn(null);

        ArticleIngestionResult result = service.ingestArticleByPublishedVersion(1L, "https://sports.sina.com.cn/nba/doc-1.shtml");

        then(articleMapper).should().insert(any(Article.class));
        assertEquals(ArticleIngestionAction.INSERTED, result.action());
    }
}
