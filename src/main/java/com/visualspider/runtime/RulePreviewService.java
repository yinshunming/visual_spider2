package com.visualspider.runtime;

import com.visualspider.admin.PreviewFieldCandidateView;
import com.visualspider.admin.PreviewFieldResultView;
import com.visualspider.admin.RulePreviewExecutionView;
import com.visualspider.admin.RulePreviewPageView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.PagePreviewSession;
import com.visualspider.persistence.RulePreviewFieldResult;
import com.visualspider.persistence.RulePreviewFieldResultMapper;
import com.visualspider.persistence.RulePreviewRun;
import com.visualspider.persistence.RulePreviewRunMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RulePreviewService {

    private final CrawlRuleMapper crawlRuleMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;
    private final CrawlRuleFieldMapper crawlRuleFieldMapper;
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;
    private final PagePreviewSessionService pagePreviewSessionService;
    private final PlaywrightService playwrightService;
    private final FieldValidationService fieldValidationService;
    private final RulePreviewRunMapper rulePreviewRunMapper;
    private final RulePreviewFieldResultMapper rulePreviewFieldResultMapper;

    public RulePreviewService(CrawlRuleMapper crawlRuleMapper,
                              CrawlRuleVersionMapper crawlRuleVersionMapper,
                              CrawlRuleFieldMapper crawlRuleFieldMapper,
                              CrawlSelectorCandidateMapper crawlSelectorCandidateMapper,
                              PagePreviewSessionService pagePreviewSessionService,
                              PlaywrightService playwrightService,
                              FieldValidationService fieldValidationService,
                              RulePreviewRunMapper rulePreviewRunMapper,
                              RulePreviewFieldResultMapper rulePreviewFieldResultMapper) {
        this.crawlRuleMapper = crawlRuleMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.crawlSelectorCandidateMapper = crawlSelectorCandidateMapper;
        this.pagePreviewSessionService = pagePreviewSessionService;
        this.playwrightService = playwrightService;
        this.fieldValidationService = fieldValidationService;
        this.rulePreviewRunMapper = rulePreviewRunMapper;
        this.rulePreviewFieldResultMapper = rulePreviewFieldResultMapper;
    }

    @Transactional
    public RulePreviewPageView preview(Long previewSessionId,
                                       Long ruleId,
                                       Map<Long, Long> candidateOverrides) {
        PagePreviewSession previewSession = requirePreviewSession(previewSessionId);
        String sourceUrl = previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();
        RulePreviewExecutionView execution = previewBySourceUrl(ruleId, sourceUrl, candidateOverrides);
        CrawlRuleVersion version = requireDraftVersion(ruleId);

        RulePreviewRun run = new RulePreviewRun();
        run.setRuleVersionId(version.getId());
        run.setPreviewSessionId(previewSessionId);
        run.setSourceUrl(sourceUrl);
        rulePreviewRunMapper.insert(run);

        for (PreviewFieldResultView fieldResult : execution.fieldResults()) {
            RulePreviewFieldResult persistResult = new RulePreviewFieldResult();
            persistResult.setPreviewRunId(run.getId());
            persistResult.setFieldId(fieldResult.fieldId());
            persistResult.setSelectorCandidateId(resolveSelectedCandidateId(fieldResult));
            persistResult.setExtractedValue(fieldResult.extractedValue());
            persistResult.setStatus(fieldResult.valid() ? "VALID" : "INVALID");
            persistResult.setValidationMessage(fieldResult.validationMessage());
            rulePreviewFieldResultMapper.insert(persistResult);
        }

        return new RulePreviewPageView(
                previewSessionId,
                execution.ruleId(),
                execution.ruleName(),
                execution.sourceUrl(),
                execution.fieldResults()
        );
    }

    public RulePreviewExecutionView previewBySourceUrl(Long ruleId, String sourceUrl) {
        return previewBySourceUrl(ruleId, sourceUrl, Map.of());
    }

    public RulePreviewExecutionView previewBySourceUrl(Long ruleId,
                                                       String sourceUrl,
                                                       Map<Long, Long> candidateOverrides) {
        CrawlRule rule = requireRule(ruleId);
        CrawlRuleVersion version = requireDraftVersion(ruleId);

        List<PreviewFieldResultView> results = new ArrayList<>();
        for (CrawlRuleField field : crawlRuleFieldMapper.findByRuleVersionId(version.getId())) {
            List<CrawlSelectorCandidate> candidates = crawlSelectorCandidateMapper.findByFieldId(field.getId());
            Long overrideId = candidateOverrides.get(field.getId());
            FieldPreviewExecution execution = executeFieldPreview(sourceUrl, field, candidates, overrideId);

            results.add(new PreviewFieldResultView(
                    field.getId(),
                    field.getFieldName(),
                    field.getFieldType(),
                    execution.extractionResult().success() ? execution.validationResult().normalizedValue() : null,
                    execution.extractionResult().success(),
                    execution.validationResult().valid(),
                    execution.validationResult().message(),
                    execution.selectedCandidate() == null ? "-" : execution.selectedCandidate().getSelectorType() + ": " + execution.selectedCandidate().getSelectorValue(),
                    candidates.stream()
                            .map(candidate -> new PreviewFieldCandidateView(
                                    candidate.getId(),
                                    candidate.getSelectorType(),
                                    candidate.getSelectorValue(),
                                    execution.selectedCandidate() != null && candidate.getId().equals(execution.selectedCandidate().getId())
                            ))
                            .toList()
            ));
        }

        return new RulePreviewExecutionView(
                ruleId,
                rule.getRuleName(),
                sourceUrl,
                results
        );
    }

    private CrawlRule requireRule(Long ruleId) {
        CrawlRule rule = crawlRuleMapper.findById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        return rule;
    }

    private CrawlRuleVersion requireDraftVersion(Long ruleId) {
        CrawlRuleVersion version = crawlRuleVersionMapper.findLatestDraftByRuleId(ruleId);
        if (version == null) {
            throw new IllegalArgumentException("规则草稿版本不存在");
        }
        return version;
    }

    private PagePreviewSession requirePreviewSession(Long previewSessionId) {
        PagePreviewSession session = pagePreviewSessionService.getSession(previewSessionId);
        if (session == null) {
            throw new IllegalArgumentException("预览会话不存在");
        }
        return session;
    }

    private CrawlSelectorCandidate resolveSelectedCandidate(List<CrawlSelectorCandidate> candidates,
                                                            Long overrideId,
                                                            String fieldType) {
        if (overrideId != null) {
            for (CrawlSelectorCandidate candidate : candidates) {
                if (candidate.getId().equals(overrideId)) {
                    return candidate;
                }
            }
        }
        if ("URL".equals(fieldType)) {
            for (CrawlSelectorCandidate candidate : candidates) {
                if (candidate.getSelectorType() != null
                        && "attribute".equalsIgnoreCase(candidate.getSelectorType().trim())) {
                    return candidate;
                }
            }
        }
        if ("DATETIME".equals(fieldType)) {
            for (CrawlSelectorCandidate candidate : candidates) {
                if (candidate.getSelectorType() != null
                        && "attribute".equalsIgnoreCase(candidate.getSelectorType().trim())) {
                    return candidate;
                }
            }
        }
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private FieldPreviewExecution executeFieldPreview(String sourceUrl,
                                                     CrawlRuleField field,
                                                     List<CrawlSelectorCandidate> candidates,
                                                     Long overrideId) {
        if (overrideId != null) {
            CrawlSelectorCandidate selected = resolveSelectedCandidate(candidates, overrideId, field.getFieldType());
            if (selected == null) {
                return new FieldPreviewExecution(
                        null,
                        new PreviewExtractionResult(false, null, null, "没有可用的 selector 候选"),
                        new FieldValidationResult(false, "", "没有可用的 selector 候选")
                );
            }
            PreviewExtractionResult extraction = playwrightService.extractValue(sourceUrl, field.getFieldType(), selected);
            FieldValidationResult validation = extraction.success()
                    ? fieldValidationService.validate(field.getFieldType(), extraction.extractedValue())
                    : new FieldValidationResult(false, "", extraction.failureReason());
            return new FieldPreviewExecution(selected, extraction, validation);
        }

        CrawlSelectorCandidate preferred = resolveSelectedCandidate(candidates, null, field.getFieldType());
        if (preferred != null) {
            PreviewExtractionResult extraction = playwrightService.extractValue(sourceUrl, field.getFieldType(), preferred);
            FieldValidationResult validation = extraction.success()
                    ? fieldValidationService.validate(field.getFieldType(), extraction.extractedValue())
                    : new FieldValidationResult(false, "", extraction.failureReason());
            if (validation.valid()) {
                return new FieldPreviewExecution(preferred, extraction, validation);
            }
        }

        for (CrawlSelectorCandidate candidate : candidates) {
            PreviewExtractionResult extraction = playwrightService.extractValue(sourceUrl, field.getFieldType(), candidate);
            FieldValidationResult validation = extraction.success()
                    ? fieldValidationService.validate(field.getFieldType(), extraction.extractedValue())
                    : new FieldValidationResult(false, "", extraction.failureReason());
            if (validation.valid()) {
                return new FieldPreviewExecution(candidate, extraction, validation);
            }
        }

        CrawlSelectorCandidate fallback = preferred != null ? preferred : (candidates.isEmpty() ? null : candidates.getFirst());
        if (fallback == null) {
            return new FieldPreviewExecution(
                    null,
                    new PreviewExtractionResult(false, null, null, "没有可用的 selector 候选"),
                    new FieldValidationResult(false, "", "没有可用的 selector 候选")
            );
        }
        PreviewExtractionResult extraction = playwrightService.extractValue(sourceUrl, field.getFieldType(), fallback);
        FieldValidationResult validation = extraction.success()
                ? fieldValidationService.validate(field.getFieldType(), extraction.extractedValue())
                : new FieldValidationResult(false, "", extraction.failureReason());
        return new FieldPreviewExecution(fallback, extraction, validation);
    }

    private record FieldPreviewExecution(
            CrawlSelectorCandidate selectedCandidate,
            PreviewExtractionResult extractionResult,
            FieldValidationResult validationResult
    ) {
    }

    private Long resolveSelectedCandidateId(PreviewFieldResultView fieldResult) {
        return fieldResult.candidates().stream()
                .filter(PreviewFieldCandidateView::selected)
                .map(PreviewFieldCandidateView::candidateId)
                .findFirst()
                .orElse(null);
    }
}
