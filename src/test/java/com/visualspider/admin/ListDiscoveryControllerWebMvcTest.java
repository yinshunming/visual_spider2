package com.visualspider.admin;

import com.visualspider.persistence.ArticleMapper;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlRunLogMapper;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.CrawlSnapshotMapper;
import com.visualspider.persistence.CrawlTaskMapper;
import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.ListDiscoveryItemMapper;
import com.visualspider.persistence.ListDiscoveryRunMapper;
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.persistence.RuleArticleMappingMapper;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRunMapper;
import com.visualspider.runtime.BatchDetailPreviewService;
import com.visualspider.runtime.ListDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RuleDraftController.class)
class ListDiscoveryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private com.visualspider.runtime.RuleDraftService ruleDraftService;
    @MockBean private ListDiscoveryService listDiscoveryService;
    @MockBean private BatchDetailPreviewService batchDetailPreviewService;
    @MockBean private DatabaseProbeMapper databaseProbeMapper;
    @MockBean private PagePreviewSessionMapper pagePreviewSessionMapper;
    @MockBean private CrawlRuleMapper crawlRuleMapper;
    @MockBean private CrawlRuleVersionMapper crawlRuleVersionMapper;
    @MockBean private CrawlRuleFieldMapper crawlRuleFieldMapper;
    @MockBean private CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;
    @MockBean private RulePreviewRunMapper rulePreviewRunMapper;
    @MockBean private RulePreviewFieldResultMapper rulePreviewFieldResultMapper;
    @MockBean private ArticleMapper articleMapper;
    @MockBean private RuleArticleMappingMapper ruleArticleMappingMapper;
    @MockBean private CrawlTaskMapper crawlTaskMapper;
    @MockBean private CrawlRunLogMapper crawlRunLogMapper;
    @MockBean private CrawlSnapshotMapper crawlSnapshotMapper;
    @MockBean private ListDiscoveryRunMapper listDiscoveryRunMapper;
    @MockBean private ListDiscoveryItemMapper listDiscoveryItemMapper;

    @Test
    void shouldRenderListDiscoveryPage() throws Exception {
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

        mockMvc.perform(get("/admin/rules/drafts/list-discovery")
                        .param("previewSessionId", "1")
                        .param("ruleId", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/list-discovery"))
                .andExpect(model().attributeExists("discoveryPage"));
    }

    @Test
    void shouldRenderReadableErrorWhenListDiscoveryFails() throws Exception {
        given(listDiscoveryService.preview(eq(1L), eq(9L)))
                .willThrow(new IllegalStateException("List discovery requires one ITEM_URL field"));

        mockMvc.perform(get("/admin/rules/drafts/list-discovery")
                        .param("previewSessionId", "1")
                        .param("ruleId", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/list-discovery"))
                .andExpect(model().attribute("discoveryError", "List discovery requires one ITEM_URL field"));
    }

    @Test
    void shouldRenderBatchDetailPreviewPage() throws Exception {
        given(batchDetailPreviewService.preview(eq(1L), eq(9L), eq(12L))).willReturn(
                new BatchDetailPreviewPageView(
                        1L,
                        9L,
                        12L,
                        "nba-list-rule",
                        "nba-detail-rule",
                        "https://sports.sina.com.cn/nba/",
                        List.of(
                                new BatchDetailPreviewItemView(
                                        0,
                                        "News 1",
                                        "https://sports.sina.com.cn/nba/doc-1.shtml",
                                        "2026-04-15",
                                        true,
                                        null,
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
                                )
                        )
                )
        );

        mockMvc.perform(get("/admin/rules/drafts/detail-batch-preview")
                        .param("previewSessionId", "1")
                        .param("listRuleId", "9")
                        .param("detailRuleId", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/detail-batch-preview"))
                .andExpect(model().attributeExists("batchPreviewPage"))
                .andExpect(model().attribute("batchPreviewError", nullValue()));
    }
}
