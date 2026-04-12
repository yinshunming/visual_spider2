package com.visualspider.runtime;

import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class RuleVersionServiceTest {

    private final CrawlRuleMapper crawlRuleMapper = mock(CrawlRuleMapper.class);
    private final CrawlRuleVersionMapper crawlRuleVersionMapper = mock(CrawlRuleVersionMapper.class);
    private final CrawlRuleFieldMapper crawlRuleFieldMapper = mock(CrawlRuleFieldMapper.class);
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper = mock(CrawlSelectorCandidateMapper.class);

    private final RuleVersionService service = new RuleVersionService(
            crawlRuleMapper,
            crawlRuleVersionMapper,
            crawlRuleFieldMapper,
            crawlSelectorCandidateMapper
    );

    @Test
    void shouldPublishVersionAndClearPreviousPublished() {
        CrawlRule rule = new CrawlRule();
        rule.setId(1L);
        rule.setRuleName("rule");
        CrawlRuleVersion version = new CrawlRuleVersion();
        version.setId(2L);
        version.setRuleId(1L);
        version.setVersionNo(2);
        version.setStatus("DRAFT");
        CrawlRuleField field = new CrawlRuleField();
        field.setId(3L);
        field.setFieldName("title");
        CrawlSelectorCandidate c1 = new CrawlSelectorCandidate();
        c1.setId(10L);
        CrawlSelectorCandidate c2 = new CrawlSelectorCandidate();
        c2.setId(11L);

        given(crawlRuleMapper.findById(1L)).willReturn(rule);
        given(crawlRuleVersionMapper.findById(2L)).willReturn(version);
        given(crawlRuleFieldMapper.findByRuleVersionId(2L)).willReturn(List.of(field));
        given(crawlSelectorCandidateMapper.findByFieldId(3L)).willReturn(List.of(c1, c2));

        service.publishVersion(1L, 2L);

        then(crawlRuleVersionMapper).should().clearPublishedStatus(1L);
        then(crawlRuleVersionMapper).should().markPublished(2L);
    }

    @Test
    void shouldRejectPublishingVersionWithoutEnoughCandidates() {
        CrawlRule rule = new CrawlRule();
        rule.setId(1L);
        CrawlRuleVersion version = new CrawlRuleVersion();
        version.setId(2L);
        version.setRuleId(1L);
        CrawlRuleField field = new CrawlRuleField();
        field.setId(3L);
        field.setFieldName("title");
        CrawlSelectorCandidate c1 = new CrawlSelectorCandidate();
        c1.setId(10L);

        given(crawlRuleMapper.findById(1L)).willReturn(rule);
        given(crawlRuleVersionMapper.findById(2L)).willReturn(version);
        given(crawlRuleFieldMapper.findByRuleVersionId(2L)).willReturn(List.of(field));
        given(crawlSelectorCandidateMapper.findByFieldId(3L)).willReturn(List.of(c1));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.publishVersion(1L, 2L));

        assertEquals("字段 title 的 selector 候选不足 2 个，无法发布", exception.getMessage());
    }
}
