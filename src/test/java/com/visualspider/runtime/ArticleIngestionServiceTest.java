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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class ArticleIngestionServiceTest {

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
            articleMapper
    );

    @Test
    void shouldInsertArticleWhenMissing() {
        CrawlRuleVersion published = new CrawlRuleVersion();
        published.setId(1L);
        published.setSourcePreviewSessionId(10L);

        RuleArticleMapping mapping = new RuleArticleMapping();
        mapping.setRuleVersionId(1L);
        mapping.setFieldId(2L);
        mapping.setArticleColumn("source_url");

        CrawlRuleField field = new CrawlRuleField();
        field.setId(2L);
        field.setFieldName("urlField");
        field.setFieldType("URL");

        CrawlSelectorCandidate candidate = new CrawlSelectorCandidate();
        candidate.setId(3L);
        candidate.setSelectorType("attribute");
        candidate.setSelectorValue("a[href=\"https://sina.cn/\"]");

        PagePreviewSession session = new PagePreviewSession();
        session.setId(10L);
        session.setRequestedUrl("https://www.sina.com.cn");
        session.setFinalUrl("https://www.sina.com.cn/");

        given(articleMappingService.requirePublishedVersion(5L)).willReturn(published);
        given(ruleArticleMappingMapper.findByRuleVersionId(1L)).willReturn(List.of(mapping));
        given(crawlRuleFieldMapper.findByRuleVersionId(1L)).willReturn(List.of(field));
        given(crawlSelectorCandidateMapper.findByFieldId(2L)).willReturn(List.of(candidate));
        given(pagePreviewSessionService.getSession(10L)).willReturn(session);
        given(playwrightService.extractValue("https://www.sina.com.cn/", "URL", candidate))
                .willReturn(new PreviewExtractionResult(true, "https://sina.cn/", candidate, null));
        given(articleMapper.findBySourceUrl("https://sina.cn/")).willReturn(null);

        String result = service.ingest(5L);

        then(articleMapper).should().insert(any(Article.class));
        assertTrue(result.contains("已插入 article"));
    }
}
