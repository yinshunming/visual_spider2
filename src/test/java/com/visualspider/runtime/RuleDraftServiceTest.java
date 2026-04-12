package com.visualspider.runtime;

import com.visualspider.admin.RuleDraftFieldForm;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.PagePreviewSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class RuleDraftServiceTest {

    private final PagePreviewSessionService pagePreviewSessionService = mock(PagePreviewSessionService.class);
    private final PlaywrightService playwrightService = mock(PlaywrightService.class);
    private final SelectorCandidateGenerator selectorCandidateGenerator = mock(SelectorCandidateGenerator.class);
    private final CrawlRuleMapper crawlRuleMapper = mock(CrawlRuleMapper.class);
    private final CrawlRuleVersionMapper crawlRuleVersionMapper = mock(CrawlRuleVersionMapper.class);
    private final CrawlRuleFieldMapper crawlRuleFieldMapper = mock(CrawlRuleFieldMapper.class);
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper = mock(CrawlSelectorCandidateMapper.class);

    private final RuleDraftService service = new RuleDraftService(
            pagePreviewSessionService,
            playwrightService,
            selectorCandidateGenerator,
            crawlRuleMapper,
            crawlRuleVersionMapper,
            crawlRuleFieldMapper,
            crawlSelectorCandidateMapper
    );

    @Test
    void shouldReuseExistingDraftVersionWhenSavingMultipleFields() {
        PagePreviewSession session = new PagePreviewSession();
        session.setId(1L);
        session.setStatus("SUCCESS");
        session.setRequestedUrl("https://www.sina.com.cn");

        CrawlRule rule = new CrawlRule();
        rule.setId(9L);
        rule.setRuleName("demo-rule");

        CrawlRuleVersion draft = new CrawlRuleVersion();
        draft.setId(12L);
        draft.setRuleId(9L);
        draft.setVersionNo(2);
        draft.setStatus("DRAFT");

        RuleDraftFieldForm form = new RuleDraftFieldForm();
        form.setPreviewSessionId(1L);
        form.setRuleId(9L);
        form.setFieldName("title");
        form.setFieldType("TEXT");
        form.setSelectedTagName("a");
        form.setSelectedText("移动客户端");
        form.setDomPath("div > a:nth-of-type(1)");

        given(pagePreviewSessionService.getSession(1L)).willReturn(session);
        given(crawlRuleMapper.findById(9L)).willReturn(rule);
        given(crawlRuleVersionMapper.findLatestDraftByRuleId(9L)).willReturn(draft);
        given(selectorCandidateGenerator.generate(any())).willReturn(java.util.List.of(
                new SelectorCandidateDraft("text", "移动客户端", 1),
                new SelectorCandidateDraft("tag", "a", 2)
        ));

        Long ruleId = service.saveDraftField(form);

        assertEquals(9L, ruleId);
        then(crawlRuleVersionMapper).should().findLatestDraftByRuleId(9L);
        then(crawlRuleVersionMapper).shouldHaveNoMoreInteractions();
    }

    @Test
    void shouldRejectOversizedSelectionText() {
        PagePreviewSession session = new PagePreviewSession();
        session.setId(1L);
        session.setStatus("SUCCESS");

        RuleDraftFieldForm form = new RuleDraftFieldForm();
        form.setPreviewSessionId(1L);
        form.setRuleName("demo-rule");
        form.setFieldName("content");
        form.setFieldType("TEXT");
        form.setSelectedTagName("div");
        form.setSelectedText("x".repeat(501));
        form.setDomPath("body > div:nth-of-type(1)");

        given(pagePreviewSessionService.getSession(1L)).willReturn(session);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.saveDraftField(form));
        assertEquals("选区过大，请选择更细粒度的元素", exception.getMessage());
    }
}
