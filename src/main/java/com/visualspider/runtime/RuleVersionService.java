package com.visualspider.runtime;

import com.visualspider.admin.RuleVersionPageView;
import com.visualspider.admin.RuleVersionSummaryView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RuleVersionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CrawlRuleMapper crawlRuleMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;
    private final CrawlRuleFieldMapper crawlRuleFieldMapper;
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;

    public RuleVersionService(CrawlRuleMapper crawlRuleMapper,
                              CrawlRuleVersionMapper crawlRuleVersionMapper,
                              CrawlRuleFieldMapper crawlRuleFieldMapper,
                              CrawlSelectorCandidateMapper crawlSelectorCandidateMapper) {
        this.crawlRuleMapper = crawlRuleMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.crawlSelectorCandidateMapper = crawlSelectorCandidateMapper;
    }

    public RuleVersionPageView buildVersionPage(Long ruleId) {
        CrawlRule rule = requireRule(ruleId);
        CrawlRuleVersion draft = crawlRuleVersionMapper.findLatestDraftByRuleId(ruleId);
        List<RuleVersionSummaryView> versions = crawlRuleVersionMapper.findByRuleId(ruleId).stream()
                .map(version -> new RuleVersionSummaryView(
                        version.getId(),
                        version.getVersionNo(),
                        version.getStatus(),
                        version.getCreatedAt() == null ? "-" : version.getCreatedAt().format(FORMATTER),
                        version.getPublishedAt() == null ? "-" : version.getPublishedAt().format(FORMATTER)
                ))
                .toList();

        return new RuleVersionPageView(
                ruleId,
                rule.getRuleName(),
                draft != null ? draft.getSourcePreviewSessionId() : (!versions.isEmpty() ? crawlRuleVersionMapper.findById(versions.getFirst().versionId()).getSourcePreviewSessionId() : null),
                draft == null ? null : draft.getId(),
                draft == null ? null : draft.getVersionNo(),
                draft == null ? null : draft.getStatus(),
                versions
        );
    }

    @Transactional
    public void publishVersion(Long ruleId, Long versionId) {
        CrawlRuleVersion version = requireVersion(ruleId, versionId);
        validateVersionPublishable(version);
        crawlRuleVersionMapper.clearPublishedStatus(ruleId);
        crawlRuleVersionMapper.markPublished(versionId);
    }

    @Transactional
    public Long createDraftFromVersion(Long ruleId, Long versionId) {
        CrawlRuleVersion source = requireVersion(ruleId, versionId);
        CrawlRuleVersion existingDraft = crawlRuleVersionMapper.findLatestDraftByRuleId(ruleId);
        if (existingDraft != null && existingDraft.getId().equals(source.getId())) {
            return existingDraft.getId();
        }

        Integer maxVersionNo = crawlRuleVersionMapper.findMaxVersionNoByRuleId(ruleId);
        CrawlRuleVersion draft = new CrawlRuleVersion();
        draft.setRuleId(ruleId);
        draft.setVersionNo(maxVersionNo == null ? 1 : maxVersionNo + 1);
        draft.setStatus("DRAFT");
        draft.setSourcePreviewSessionId(source.getSourcePreviewSessionId());
        crawlRuleVersionMapper.insert(draft);

        for (CrawlRuleField sourceField : crawlRuleFieldMapper.findByRuleVersionId(source.getId())) {
            CrawlRuleField targetField = new CrawlRuleField();
            targetField.setRuleVersionId(draft.getId());
            targetField.setFieldName(sourceField.getFieldName());
            targetField.setFieldType(sourceField.getFieldType());
            targetField.setSelectedTagName(sourceField.getSelectedTagName());
            targetField.setSelectedText(sourceField.getSelectedText());
            targetField.setDomPath(sourceField.getDomPath());
            crawlRuleFieldMapper.insert(targetField);

            for (CrawlSelectorCandidate candidate : crawlSelectorCandidateMapper.findByFieldId(sourceField.getId())) {
                CrawlSelectorCandidate newCandidate = new CrawlSelectorCandidate();
                newCandidate.setFieldId(targetField.getId());
                newCandidate.setSelectorType(candidate.getSelectorType());
                newCandidate.setSelectorValue(candidate.getSelectorValue());
                newCandidate.setPriority(candidate.getPriority());
                crawlSelectorCandidateMapper.insert(newCandidate);
            }
        }
        return draft.getId();
    }

    private CrawlRule requireRule(Long ruleId) {
        CrawlRule rule = crawlRuleMapper.findById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        return rule;
    }

    private CrawlRuleVersion requireVersion(Long ruleId, Long versionId) {
        CrawlRuleVersion version = crawlRuleVersionMapper.findById(versionId);
        if (version == null || !ruleId.equals(version.getRuleId())) {
            throw new IllegalArgumentException("规则版本不存在");
        }
        return version;
    }

    private void validateVersionPublishable(CrawlRuleVersion version) {
        List<CrawlRuleField> fields = crawlRuleFieldMapper.findByRuleVersionId(version.getId());
        if (fields.isEmpty()) {
            throw new IllegalStateException("规则版本没有字段，无法发布");
        }
        for (CrawlRuleField field : fields) {
            List<CrawlSelectorCandidate> candidates = crawlSelectorCandidateMapper.findByFieldId(field.getId());
            if (candidates.size() < 2) {
                throw new IllegalStateException("字段 " + field.getFieldName() + " 的 selector 候选不足 2 个，无法发布");
            }
        }
    }
}
