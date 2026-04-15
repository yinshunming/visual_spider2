package com.visualspider.runtime;

import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.ListDiscoveryItemMapper;
import com.visualspider.persistence.ListDiscoveryRunMapper;
import com.visualspider.persistence.PagePreviewSession;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ListDiscoveryServiceTest {

    private final CrawlRuleMapper crawlRuleMapper = mock(CrawlRuleMapper.class);
    private final CrawlRuleVersionMapper crawlRuleVersionMapper = mock(CrawlRuleVersionMapper.class);
    private final CrawlRuleFieldMapper crawlRuleFieldMapper = mock(CrawlRuleFieldMapper.class);
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper = mock(CrawlSelectorCandidateMapper.class);
    private final PagePreviewSessionService pagePreviewSessionService = mock(PagePreviewSessionService.class);
    private final PlaywrightService playwrightService = mock(PlaywrightService.class);
    private final ListDiscoveryRunMapper listDiscoveryRunMapper = mock(ListDiscoveryRunMapper.class);
    private final ListDiscoveryItemMapper listDiscoveryItemMapper = mock(ListDiscoveryItemMapper.class);

    private final ListDiscoveryService service = new ListDiscoveryService(
            crawlRuleMapper,
            crawlRuleVersionMapper,
            crawlRuleFieldMapper,
            crawlSelectorCandidateMapper,
            pagePreviewSessionService,
            playwrightService,
            listDiscoveryRunMapper,
            listDiscoveryItemMapper
    );

    @Test
    void shouldFailWhenItemUrlFieldMissing() {
        CrawlRule rule = new CrawlRule();
        rule.setId(9L);
        rule.setRuleName("nba-list-rule");

        CrawlRuleVersion draft = new CrawlRuleVersion();
        draft.setId(11L);
        draft.setRuleId(9L);
        draft.setStatus("DRAFT");

        PagePreviewSession session = new PagePreviewSession();
        session.setId(1L);
        session.setStatus("SUCCESS");
        session.setRequestedUrl("https://sports.sina.com.cn/nba/");

        CrawlRuleField onlyTitle = new CrawlRuleField();
        onlyTitle.setId(21L);
        onlyTitle.setFieldName("titleField");
        onlyTitle.setFieldRole("ITEM_TITLE");

        given(crawlRuleMapper.findById(9L)).willReturn(rule);
        given(crawlRuleVersionMapper.findLatestDraftByRuleId(9L)).willReturn(draft);
        given(pagePreviewSessionService.getSession(1L)).willReturn(session);
        given(crawlRuleFieldMapper.findByRuleVersionId(11L)).willReturn(List.of(onlyTitle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.preview(1L, 9L));

        assertEquals("List discovery requires one ITEM_URL field", exception.getMessage());
    }

    @Test
    void shouldReturnMultipleItemsWhenListFieldsConfigured() {
        CrawlRule rule = new CrawlRule();
        rule.setId(9L);
        rule.setRuleName("nba-list-rule");

        CrawlRuleVersion draft = new CrawlRuleVersion();
        draft.setId(11L);
        draft.setRuleId(9L);
        draft.setStatus("DRAFT");

        PagePreviewSession session = new PagePreviewSession();
        session.setId(1L);
        session.setStatus("SUCCESS");
        session.setRequestedUrl("https://sports.sina.com.cn/nba/");
        session.setFinalUrl("https://sports.sina.com.cn/nba/");

        CrawlRuleField titleField = buildField(21L, "titleField", "TEXT", "ITEM_TITLE");
        CrawlRuleField urlField = buildField(22L, "urlField", "URL", "ITEM_URL");
        CrawlRuleField timeField = buildField(23L, "timeField", "TEXT", "ITEM_TIME");

        CrawlSelectorCandidate classCandidate = buildCandidate("css_class", "a.news-link", 1);
        CrawlSelectorCandidate timeCandidate = buildCandidate("css_class", "span.time", 1);

        given(crawlRuleMapper.findById(9L)).willReturn(rule);
        given(crawlRuleVersionMapper.findLatestDraftByRuleId(9L)).willReturn(draft);
        given(pagePreviewSessionService.getSession(1L)).willReturn(session);
        given(crawlRuleFieldMapper.findByRuleVersionId(11L)).willReturn(List.of(titleField, urlField, timeField));
        given(crawlSelectorCandidateMapper.findByFieldId(21L)).willReturn(List.of(classCandidate));
        given(crawlSelectorCandidateMapper.findByFieldId(22L)).willReturn(List.of(classCandidate));
        given(crawlSelectorCandidateMapper.findByFieldId(23L)).willReturn(List.of(timeCandidate));
        given(playwrightService.extractValues("https://sports.sina.com.cn/nba/", "TEXT", classCandidate))
                .willReturn(List.of("News 1", "News 2"));
        given(playwrightService.extractValues("https://sports.sina.com.cn/nba/", "URL", classCandidate))
                .willReturn(List.of("https://sports.sina.com.cn/nba/doc-1.shtml", "https://sports.sina.com.cn/nba/doc-2.shtml"));
        given(playwrightService.extractValues("https://sports.sina.com.cn/nba/", "TEXT", timeCandidate))
                .willReturn(List.of("2026-04-15", "2026-04-14"));

        var page = service.preview(1L, 9L);

        assertEquals(2, page.items().size());
        assertEquals("News 1", page.items().getFirst().titleText());
        assertEquals("https://sports.sina.com.cn/nba/doc-1.shtml", page.items().getFirst().detailUrl());
        verify(listDiscoveryRunMapper).insert(any());
        verify(listDiscoveryItemMapper, times(2)).insert(any());
    }

    private CrawlRuleField buildField(Long id, String name, String type, String role) {
        CrawlRuleField field = new CrawlRuleField();
        field.setId(id);
        field.setFieldName(name);
        field.setFieldType(type);
        field.setFieldRole(role);
        return field;
    }

    private CrawlSelectorCandidate buildCandidate(String type, String value, int priority) {
        CrawlSelectorCandidate candidate = new CrawlSelectorCandidate();
        candidate.setSelectorType(type);
        candidate.setSelectorValue(value);
        candidate.setPriority(priority);
        return candidate;
    }
}
