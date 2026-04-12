package com.visualspider.runtime;

import com.visualspider.admin.RuleDraftFieldForm;
import com.visualspider.admin.RuleDraftPageView;
import com.visualspider.admin.RuleFieldSummaryView;
import com.visualspider.admin.SelectableElementView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.PagePreviewSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleDraftService {

    private final PagePreviewSessionService pagePreviewSessionService;
    private final PlaywrightService playwrightService;
    private final SelectorCandidateGenerator selectorCandidateGenerator;
    private final CrawlRuleMapper crawlRuleMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;
    private final CrawlRuleFieldMapper crawlRuleFieldMapper;
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;

    public RuleDraftService(PagePreviewSessionService pagePreviewSessionService,
                            PlaywrightService playwrightService,
                            SelectorCandidateGenerator selectorCandidateGenerator,
                            CrawlRuleMapper crawlRuleMapper,
                            CrawlRuleVersionMapper crawlRuleVersionMapper,
                            CrawlRuleFieldMapper crawlRuleFieldMapper,
                            CrawlSelectorCandidateMapper crawlSelectorCandidateMapper) {
        this.pagePreviewSessionService = pagePreviewSessionService;
        this.playwrightService = playwrightService;
        this.selectorCandidateGenerator = selectorCandidateGenerator;
        this.crawlRuleMapper = crawlRuleMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.crawlSelectorCandidateMapper = crawlSelectorCandidateMapper;
    }

    public RuleDraftPageView buildDraftPage(Long previewSessionId, Long ruleId) {
        PagePreviewSession previewSession = requirePreviewSession(previewSessionId);
        CrawlRule rule = ruleId == null ? null : crawlRuleMapper.findById(ruleId);
        CrawlRuleVersion draftVersion = ruleId == null ? null : crawlRuleVersionMapper.findLatestDraftByRuleId(ruleId);

        List<SelectableElementView> selectableElements = playwrightService.inspectSelectableElements(resolvePreviewUrl(previewSession))
                .stream()
                .map(this::toView)
                .toList();

        List<RuleFieldSummaryView> fields = draftVersion == null ? List.of() : buildFieldSummaries(draftVersion.getId());

        return new RuleDraftPageView(
                previewSessionId,
                previewSession.getPageTitle(),
                resolvePreviewUrl(previewSession),
                "/admin/preview-sessions/" + previewSessionId + "/screenshot",
                rule == null ? null : rule.getId(),
                rule == null ? null : rule.getRuleName(),
                selectableElements,
                fields
        );
    }

    @Transactional
    public Long saveDraftField(RuleDraftFieldForm fieldForm) {
        PagePreviewSession previewSession = requirePreviewSession(fieldForm.getPreviewSessionId());

        CrawlRule rule = resolveRule(fieldForm, previewSession);
        CrawlRuleVersion version = resolveDraftVersion(rule, previewSession.getId());

        CrawlRuleField field = new CrawlRuleField();
        field.setRuleVersionId(version.getId());
        field.setFieldName(fieldForm.getFieldName().trim());
        field.setFieldType(fieldForm.getFieldType().trim());
        field.setSelectedTagName(fieldForm.getSelectedTagName().trim());
        field.setSelectedText(fieldForm.getSelectedText().trim());
        field.setDomPath(fieldForm.getDomPath().trim());
        crawlRuleFieldMapper.insert(field);

        SelectableElement selectedElement = new SelectableElement(
                -1,
                fieldForm.getSelectedTagName(),
                fieldForm.getSelectedText(),
                fieldForm.getDomPath(),
                fieldForm.getElementIdValue(),
                fieldForm.getClassNames(),
                fieldForm.getHrefValue(),
                fieldForm.getTitleValue(),
                fieldForm.getDateTimeValue(),
                0,
                0,
                0,
                0
        );

        for (SelectorCandidateDraft candidateDraft : selectorCandidateGenerator.generate(selectedElement)) {
            CrawlSelectorCandidate candidate = new CrawlSelectorCandidate();
            candidate.setFieldId(field.getId());
            candidate.setSelectorType(candidateDraft.selectorType());
            candidate.setSelectorValue(candidateDraft.selectorValue());
            candidate.setPriority(candidateDraft.priority());
            crawlSelectorCandidateMapper.insert(candidate);
        }
        return rule.getId();
    }

    private CrawlRule resolveRule(RuleDraftFieldForm fieldForm, PagePreviewSession previewSession) {
        if (fieldForm.getRuleId() != null) {
            CrawlRule existing = crawlRuleMapper.findById(fieldForm.getRuleId());
            if (existing != null) {
                return existing;
            }
        }

        CrawlRule rule = new CrawlRule();
        rule.setRuleName(fieldForm.getRuleName().trim());
        rule.setSourcePreviewSessionId(previewSession.getId());
        crawlRuleMapper.insert(rule);
        return rule;
    }

    private CrawlRuleVersion resolveDraftVersion(CrawlRule rule, Long previewSessionId) {
        CrawlRuleVersion existing = crawlRuleVersionMapper.findLatestDraftByRuleId(rule.getId());
        if (existing != null) {
            return existing;
        }

        CrawlRuleVersion version = new CrawlRuleVersion();
        version.setRuleId(rule.getId());
        version.setVersionNo(1);
        version.setStatus("DRAFT");
        version.setSourcePreviewSessionId(previewSessionId);
        crawlRuleVersionMapper.insert(version);
        return version;
    }

    private List<RuleFieldSummaryView> buildFieldSummaries(Long ruleVersionId) {
        List<RuleFieldSummaryView> summaries = new ArrayList<>();
        for (CrawlRuleField field : crawlRuleFieldMapper.findByRuleVersionId(ruleVersionId)) {
            List<String> selectorSummaries = crawlSelectorCandidateMapper.findByFieldId(field.getId())
                    .stream()
                    .map(candidate -> candidate.getSelectorType() + ": " + candidate.getSelectorValue())
                    .toList();
            summaries.add(new RuleFieldSummaryView(
                    field.getId(),
                    field.getFieldName(),
                    field.getFieldType(),
                    field.getSelectedText(),
                    field.getDomPath(),
                    selectorSummaries
            ));
        }
        return summaries;
    }

    private PagePreviewSession requirePreviewSession(Long previewSessionId) {
        PagePreviewSession session = pagePreviewSessionService.getSession(previewSessionId);
        if (session == null) {
            throw new IllegalArgumentException("预览会话不存在");
        }
        if (!"SUCCESS".equalsIgnoreCase(session.getStatus())) {
            throw new IllegalArgumentException("预览会话尚未成功完成，无法创建规则草稿");
        }
        return session;
    }

    private String resolvePreviewUrl(PagePreviewSession previewSession) {
        return previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();
    }

    private SelectableElementView toView(SelectableElement element) {
        return new SelectableElementView(
                element.elementIndex(),
                element.tagName(),
                element.text(),
                element.domPath(),
                element.elementIdValue(),
                element.classNames(),
                element.hrefValue(),
                element.titleValue(),
                element.dateTimeValue(),
                element.topPercent(),
                element.leftPercent(),
                element.widthPercent(),
                element.heightPercent()
        );
    }
}

