package com.visualspider.admin;

import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRunMapper;
import com.visualspider.runtime.RulePreviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RulePreviewController.class)
class RulePreviewControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RulePreviewService rulePreviewService;

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
    void shouldRenderPreviewPage() throws Exception {
        given(rulePreviewService.preview(eq(1L), eq(9L), anyMap())).willReturn(
                new RulePreviewPageView(
                        1L,
                        9L,
                        "sina-home-draft",
                        "https://www.sina.com.cn/",
                        List.of(
                                new PreviewFieldResultView(
                                        2L,
                                        "headline",
                                        "TEXT",
                                        "新浪首页",
                                        true,
                                        true,
                                        "校验通过",
                                        "css: #main_title",
                                        List.of(new PreviewFieldCandidateView(5L, "css", "#main_title", true))
                                )
                        )
                )
        );

        mockMvc.perform(get("/admin/rules/previews")
                        .param("previewSessionId", "1")
                        .param("ruleId", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rule-preview"))
                .andExpect(model().attributeExists("previewPage"));
    }
}

