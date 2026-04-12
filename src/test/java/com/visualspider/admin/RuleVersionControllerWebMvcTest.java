package com.visualspider.admin;

import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRunMapper;
import com.visualspider.runtime.RuleVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RuleVersionController.class)
class RuleVersionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleVersionService ruleVersionService;

    @MockBean
    private DatabaseProbeMapper databaseProbeMapper;

    @MockBean
    private PagePreviewSessionMapper pagePreviewSessionMapper;

    @MockBean
    private CrawlRuleMapper crawlRuleMapper;

    @MockBean
    private CrawlRuleVersionMapper crawlRuleVersionMapper;

    @MockBean
    private CrawlRuleFieldMapper crawlRuleFieldMapper;

    @MockBean
    private CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;

    @MockBean
    private RulePreviewRunMapper rulePreviewRunMapper;

    @MockBean
    private RulePreviewFieldResultMapper rulePreviewFieldResultMapper;

    @Test
    void shouldRenderVersionPage() throws Exception {
        given(ruleVersionService.buildVersionPage(eq(9L))).willReturn(
                new RuleVersionPageView(
                        9L,
                        "sina-home-rule",
                        1L,
                        12L,
                        2,
                        "DRAFT",
                        List.of(
                                new RuleVersionSummaryView(10L, 2, "DRAFT", "2026-04-12 10:00:00", "-"),
                                new RuleVersionSummaryView(8L, 1, "PUBLISHED", "2026-04-12 09:00:00", "2026-04-12 09:20:00")
                        )
                )
        );

        mockMvc.perform(get("/admin/rules/9/versions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rule-versions"))
                .andExpect(model().attributeExists("versionPage"));
    }

    @Test
    void shouldRedirectAfterPublish() throws Exception {
        willDoNothing().given(ruleVersionService).publishVersion(9L, 8L);

        mockMvc.perform(post("/admin/rules/9/versions/8/publish"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rules/9/versions"));
    }
}
