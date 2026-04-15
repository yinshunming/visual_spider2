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
import com.visualspider.runtime.ListDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RuleListController.class)
class RuleListControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CrawlRuleMapper crawlRuleMapper;
    @MockBean private CrawlRuleVersionMapper crawlRuleVersionMapper;
    @MockBean private DatabaseProbeMapper databaseProbeMapper;
    @MockBean private PagePreviewSessionMapper pagePreviewSessionMapper;
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
    @MockBean private ListDiscoveryService listDiscoveryService;

    @Test
    void shouldRenderRuleListPage() throws Exception {
        given(crawlRuleMapper.findAll()).willReturn(List.of());

        mockMvc.perform(get("/admin/rules"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rule-list"))
                .andExpect(model().attributeExists("rules"));
    }
}
