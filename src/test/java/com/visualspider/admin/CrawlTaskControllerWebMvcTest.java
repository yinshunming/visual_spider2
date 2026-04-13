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
import com.visualspider.persistence.PagePreviewSessionMapper;
import com.visualspider.persistence.RuleArticleMappingMapper;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRunMapper;
import com.visualspider.scheduler.CrawlTaskService;
import com.visualspider.runtime.RuleVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

@WebMvcTest(CrawlTaskController.class)
class CrawlTaskControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrawlTaskService crawlTaskService;

    @MockBean
    private RuleVersionService ruleVersionService;

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

    @Test
    void shouldRenderTaskForm() throws Exception {
        given(ruleVersionService.findPublishedVersionOptions()).willReturn(
                List.of(new TaskOptionView(11L, "demo-rule", 1))
        );

        mockMvc.perform(get("/admin/tasks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task-form"))
                .andExpect(model().attributeExists("taskForm"))
                .andExpect(model().attributeExists("publishedVersions"));
    }

    @Test
    void shouldRedirectAfterSave() throws Exception {
        given(ruleVersionService.findPublishedVersionOptions()).willReturn(List.of());
        given(crawlTaskService.saveTask(any())).willReturn(3L);

        mockMvc.perform(post("/admin/tasks")
                        .param("taskName", "demo-task")
                        .param("urlTemplate", "https://www.sina.com.cn")
                        .param("ruleVersionId", "11")
                        .param("cronExpression", "0 0/5 * * * ?")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tasks"));
    }
}
