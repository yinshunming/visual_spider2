package com.visualspider.admin;

import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRunMapper;
import com.visualspider.runtime.RuleDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RuleDraftController.class)
class RuleDraftControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleDraftService ruleDraftService;

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
    void shouldRenderDraftPage() throws Exception {
        given(ruleDraftService.buildDraftPage(eq(1L), eq(null))).willReturn(
                new RuleDraftPageView(
                        1L,
                        "新浪首页",
                        "https://www.sina.com.cn/",
                        "/admin/preview-sessions/1/screenshot",
                        null,
                        null,
                        List.of(),
                        List.of()
                )
        );

        mockMvc.perform(get("/admin/rules/drafts/new").param("previewSessionId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rule-draft"))
                .andExpect(model().attributeExists("pageView"))
                .andExpect(model().attributeExists("fieldForm"));
    }

    @Test
    void shouldRedirectAfterSavingField() throws Exception {
        given(ruleDraftService.saveDraftField(any(RuleDraftFieldForm.class))).willReturn(9L);

        mockMvc.perform(post("/admin/rules/drafts/fields")
                        .param("previewSessionId", "1")
                        .param("ruleName", "sina-home")
                        .param("fieldName", "title")
                        .param("fieldType", "TEXT")
                        .param("selectedTagName", "h1")
                        .param("selectedText", "新浪首页")
                        .param("domPath", "body > h1:nth-of-type(1)"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rules/drafts/new?previewSessionId=1&ruleId=9"));
    }
}
