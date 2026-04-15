package com.visualspider.runtime;

import com.visualspider.admin.ListDiscoveryItemView;
import com.visualspider.admin.ListDiscoveryPageView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleField;
import com.visualspider.persistence.CrawlRuleFieldMapper;
import com.visualspider.persistence.CrawlRuleMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlSelectorCandidate;
import com.visualspider.persistence.CrawlSelectorCandidateMapper;
import com.visualspider.persistence.ListDiscoveryItem;
import com.visualspider.persistence.ListDiscoveryItemMapper;
import com.visualspider.persistence.ListDiscoveryRun;
import com.visualspider.persistence.ListDiscoveryRunMapper;
import com.visualspider.persistence.PagePreviewSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ListDiscoveryService {

    private final CrawlRuleMapper crawlRuleMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;
    private final CrawlRuleFieldMapper crawlRuleFieldMapper;
    private final CrawlSelectorCandidateMapper crawlSelectorCandidateMapper;
    private final PagePreviewSessionService pagePreviewSessionService;
    private final PlaywrightService playwrightService;
    private final ListDiscoveryRunMapper listDiscoveryRunMapper;
    private final ListDiscoveryItemMapper listDiscoveryItemMapper;

    public ListDiscoveryService(CrawlRuleMapper crawlRuleMapper,
                                CrawlRuleVersionMapper crawlRuleVersionMapper,
                                CrawlRuleFieldMapper crawlRuleFieldMapper,
                                CrawlSelectorCandidateMapper crawlSelectorCandidateMapper,
                                PagePreviewSessionService pagePreviewSessionService,
                                PlaywrightService playwrightService,
                                ListDiscoveryRunMapper listDiscoveryRunMapper,
                                ListDiscoveryItemMapper listDiscoveryItemMapper) {
        this.crawlRuleMapper = crawlRuleMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
        this.crawlRuleFieldMapper = crawlRuleFieldMapper;
        this.crawlSelectorCandidateMapper = crawlSelectorCandidateMapper;
        this.pagePreviewSessionService = pagePreviewSessionService;
        this.playwrightService = playwrightService;
        this.listDiscoveryRunMapper = listDiscoveryRunMapper;
        this.listDiscoveryItemMapper = listDiscoveryItemMapper;
    }

    @Transactional
    public ListDiscoveryPageView preview(Long previewSessionId, Long ruleId) {
        CrawlRule rule = requireRule(ruleId);
        CrawlRuleVersion draftVersion = requireDraftVersion(ruleId);
        PagePreviewSession previewSession = requirePreviewSession(previewSessionId);
        String sourceUrl = previewSession.getFinalUrl() != null ? previewSession.getFinalUrl() : previewSession.getRequestedUrl();

        CrawlRuleField titleField = findFieldByRole(draftVersion.getId(), "ITEM_TITLE");
        CrawlRuleField urlField = findFieldByRole(draftVersion.getId(), "ITEM_URL");
        CrawlRuleField timeField = findFieldByRole(draftVersion.getId(), "ITEM_TIME");

        if (urlField == null) {
            throw new IllegalStateException("List discovery requires one ITEM_URL field");
        }

        List<String> titleValues = titleField == null ? List.of() : extractValues(sourceUrl, titleField);
        List<String> urlValues = extractValues(sourceUrl, urlField);
        List<String> timeValues = timeField == null ? List.of() : extractValues(sourceUrl, timeField);

        ListDiscoveryRun run = new ListDiscoveryRun();
        run.setRuleVersionId(draftVersion.getId());
        run.setSourceUrl(sourceUrl);
        listDiscoveryRunMapper.insert(run);

        int max = Math.max(urlValues.size(), Math.max(titleValues.size(), timeValues.size()));
        List<ListDiscoveryItemView> items = new ArrayList<>();
        for (int index = 0; index < max; index++) {
            String detailUrl = valueAt(urlValues, index);
            if (detailUrl == null || detailUrl.isBlank()) {
                continue;
            }

            ListDiscoveryItem item = new ListDiscoveryItem();
            item.setRunId(run.getId());
            item.setItemIndex(index);
            item.setTitleText(valueAt(titleValues, index));
            item.setDetailUrl(detailUrl);
            item.setTimeText(valueAt(timeValues, index));
            listDiscoveryItemMapper.insert(item);

            items.add(new ListDiscoveryItemView(index, item.getTitleText(), item.getDetailUrl(), item.getTimeText()));
        }

        return new ListDiscoveryPageView(
                previewSessionId,
                ruleId,
                rule.getRuleName(),
                sourceUrl,
                items
        );
    }

    private List<String> extractValues(String sourceUrl, CrawlRuleField field) {
        CrawlSelectorCandidate candidate = selectCandidate(field);
        return playwrightService.extractValues(sourceUrl, field.getFieldType(), candidate);
    }

    private CrawlSelectorCandidate selectCandidate(CrawlRuleField field) {
        List<CrawlSelectorCandidate> candidates = crawlSelectorCandidateMapper.findByFieldId(field.getId());
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Field " + field.getFieldName() + " has no selector candidates");
        }

        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((CrawlSelectorCandidate candidate) -> candidatePriority(field, candidate))
                        .thenComparing(candidate -> candidate.getPriority() == null ? Integer.MAX_VALUE : candidate.getPriority()))
                .map(candidate -> normalizeCandidate(field, candidate))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Field " + field.getFieldName() + " has no usable selector candidates"));
    }

    private int candidatePriority(CrawlRuleField field, CrawlSelectorCandidate candidate) {
        boolean listField = field.getFieldRole() != null && field.getFieldRole().startsWith("ITEM_");
        String type = candidate.getSelectorType() == null ? "" : candidate.getSelectorType().toLowerCase();

        if (listField) {
            return switch (type) {
                case "css_class" -> 0;
                case "dom_path" -> 1;
                case "tag" -> 2;
                case "css" -> 3;
                case "attribute" -> 4;
                case "text" -> 5;
                default -> 6;
            };
        }

        if ("URL".equalsIgnoreCase(field.getFieldType())) {
            return switch (type) {
                case "attribute" -> 0;
                case "css", "css_class", "dom_path" -> 1;
                case "text" -> 2;
                case "tag" -> 3;
                default -> 4;
            };
        }

        return switch (type) {
            case "css", "css_class", "dom_path" -> 0;
            case "text" -> 1;
            case "attribute" -> 2;
            case "tag" -> 3;
            default -> 4;
        };
    }

    private CrawlSelectorCandidate normalizeCandidate(CrawlRuleField field, CrawlSelectorCandidate candidate) {
        boolean listField = field.getFieldRole() != null && field.getFieldRole().startsWith("ITEM_");
        if (!listField || !"dom_path".equalsIgnoreCase(candidate.getSelectorType())) {
            return candidate;
        }

        CrawlSelectorCandidate normalized = new CrawlSelectorCandidate();
        normalized.setId(candidate.getId());
        normalized.setFieldId(candidate.getFieldId());
        normalized.setSelectorType(candidate.getSelectorType());
        normalized.setPriority(candidate.getPriority());
        normalized.setSelectorValue(candidate.getSelectorValue().replaceAll(":nth-of-type\\(\\d+\\)", ""));
        return normalized;
    }

    private CrawlRule requireRule(Long ruleId) {
        CrawlRule rule = crawlRuleMapper.findById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule does not exist");
        }
        return rule;
    }

    private CrawlRuleVersion requireDraftVersion(Long ruleId) {
        CrawlRuleVersion version = crawlRuleVersionMapper.findLatestDraftByRuleId(ruleId);
        if (version == null) {
            throw new IllegalArgumentException("Draft version does not exist");
        }
        return version;
    }

    private PagePreviewSession requirePreviewSession(Long previewSessionId) {
        PagePreviewSession session = pagePreviewSessionService.getSession(previewSessionId);
        if (session == null) {
            throw new IllegalArgumentException("Preview session does not exist");
        }
        return session;
    }

    private CrawlRuleField findFieldByRole(Long versionId, String role) {
        return crawlRuleFieldMapper.findByRuleVersionId(versionId).stream()
                .filter(field -> role.equalsIgnoreCase(field.getFieldRole()))
                .findFirst()
                .orElse(null);
    }

    private String valueAt(List<String> values, int index) {
        return index >= 0 && index < values.size() ? values.get(index) : null;
    }
}
