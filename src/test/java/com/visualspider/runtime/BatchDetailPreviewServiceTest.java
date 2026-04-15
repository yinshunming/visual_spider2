package com.visualspider.runtime;

import com.visualspider.admin.BatchDetailPreviewItemView;
import com.visualspider.admin.ListDiscoveryItemView;
import com.visualspider.admin.ListDiscoveryPageView;
import com.visualspider.admin.PreviewFieldResultView;
import com.visualspider.admin.RulePreviewExecutionView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class BatchDetailPreviewServiceTest {

    private final ListDiscoveryService listDiscoveryService = mock(ListDiscoveryService.class);
    private final RulePreviewService rulePreviewService = mock(RulePreviewService.class);
    private final CrawlRuleMapper crawlRuleMapper = mock(CrawlRuleMapper.class);

    private final BatchDetailPreviewService service = new BatchDetailPreviewService(
            listDiscoveryService,
            rulePreviewService,
            crawlRuleMapper
    );

    @Test
    void shouldBuildBatchPreviewFromListDiscoveryAndDetailRule() {
        CrawlRule detailRule = new CrawlRule();
        detailRule.setId(12L);
        detailRule.setRuleName("nba-detail-rule");

        given(crawlRuleMapper.findById(12L)).willReturn(detailRule);
        given(listDiscoveryService.preview(eq(1L), eq(9L))).willReturn(
                new ListDiscoveryPageView(
                        1L,
                        9L,
                        "nba-list-rule",
                        "https://sports.sina.com.cn/nba/",
                        List.of(
                                new ListDiscoveryItemView(0, "News 1", "https://sports.sina.com.cn/nba/doc-1.shtml", "2026-04-15"),
                                new ListDiscoveryItemView(1, "News 2", "https://sports.sina.com.cn/nba/doc-2.shtml", "2026-04-14")
                        )
                )
        );
        given(rulePreviewService.previewBySourceUrl(eq(12L), eq("https://sports.sina.com.cn/nba/doc-1.shtml")))
                .willReturn(new RulePreviewExecutionView(
                        12L,
                        "nba-detail-rule",
                        "https://sports.sina.com.cn/nba/doc-1.shtml",
                        List.of(
                                new PreviewFieldResultView(
                                        101L,
                                        "title",
                                        "TEXT",
                                        "First article",
                                        true,
                                        true,
                                        "ok",
                                        "css: h1",
                                        List.of()
                                )
                        )
                ));
        given(rulePreviewService.previewBySourceUrl(eq(12L), eq("https://sports.sina.com.cn/nba/doc-2.shtml")))
                .willReturn(new RulePreviewExecutionView(
                        12L,
                        "nba-detail-rule",
                        "https://sports.sina.com.cn/nba/doc-2.shtml",
                        List.of(
                                new PreviewFieldResultView(
                                        102L,
                                        "content",
                                        "HTML",
                                        "<p>Second article</p>",
                                        true,
                                        true,
                                        "ok",
                                        "css: .article",
                                        List.of()
                                )
                        )
                ));

        var page = service.preview(1L, 9L, 12L);

        assertEquals("nba-list-rule", page.listRuleName());
        assertEquals("nba-detail-rule", page.detailRuleName());
        assertEquals(2, page.items().size());
        assertTrue(page.items().getFirst().success());
        assertEquals("First article", page.items().getFirst().fieldResults().getFirst().extractedValue());
    }

    @Test
    void shouldKeepItemLevelErrorWhenDetailPreviewFails() {
        CrawlRule detailRule = new CrawlRule();
        detailRule.setId(12L);
        detailRule.setRuleName("nba-detail-rule");

        given(crawlRuleMapper.findById(12L)).willReturn(detailRule);
        given(listDiscoveryService.preview(eq(1L), eq(9L))).willReturn(
                new ListDiscoveryPageView(
                        1L,
                        9L,
                        "nba-list-rule",
                        "https://sports.sina.com.cn/nba/",
                        List.of(new ListDiscoveryItemView(0, "Broken news", "https://sports.sina.com.cn/nba/doc-bad.shtml", "2026-04-15"))
                )
        );
        given(rulePreviewService.previewBySourceUrl(eq(12L), eq("https://sports.sina.com.cn/nba/doc-bad.shtml")))
                .willThrow(new IllegalStateException("Detail rule draft does not exist"));

        var page = service.preview(1L, 9L, 12L);
        BatchDetailPreviewItemView item = page.items().getFirst();

        assertFalse(item.success());
        assertEquals("Detail rule draft does not exist", item.errorMessage());
        assertTrue(item.fieldResults().isEmpty());
    }
}
