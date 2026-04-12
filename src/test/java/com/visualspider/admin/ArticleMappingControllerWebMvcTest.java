package com.visualspider.admin;

import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.DatabaseProbeMapper;
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.persistence.ArticleMapper;
import com.visualspider.persistence.RuleArticleMappingMapper;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRunMapper;
import com.visualspider.runtime.ArticleIngestionService;
import com.visualspider.runtime.ArticleMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ArticleMappingController.class)
class ArticleMappingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleMappingService articleMappingService;

    @MockBean
    private ArticleIngestionService articleIngestionService;

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

    @MockBean
    private RuleArticleMappingMapper ruleArticleMappingMapper;

    @MockBean
    private ArticleMapper articleMapper;

    @Test
    void shouldRenderArticleMappingPage() throws Exception {
        given(articleMappingService.buildPage(eq(5L), eq(null))).willReturn(
                new ArticleMappingPageView(
                        5L,
                        "m6-rule",
                        10L,
                        1,
                        List.of(new ArticleFieldOptionView(2L, "titleField", "TEXT")),
                        Map.of(),
                        null
                )
        );
        given(articleMappingService.buildForm(eq(5L))).willReturn(new ArticleMappingForm());

        mockMvc.perform(get("/admin/rules/5/article-mappings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/article-mapping"))
                .andExpect(model().attributeExists("mappingPage"))
                .andExpect(model().attributeExists("mappingForm"));
    }

    @Test
    void shouldRedirectAfterSavingMappings() throws Exception {
        willDoNothing().given(articleMappingService).saveMappings(eq(5L), any(ArticleMappingForm.class));

        mockMvc.perform(post("/admin/rules/5/article-mappings")
                        .param("sourceUrlFieldId", "1")
                        .param("titleFieldId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rules/5/article-mappings"));
    }
}
