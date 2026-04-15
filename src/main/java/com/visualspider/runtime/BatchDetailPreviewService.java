package com.visualspider.runtime;

import com.visualspider.admin.BatchDetailPreviewItemView;
import com.visualspider.admin.BatchDetailPreviewPageView;
import com.visualspider.admin.ListDiscoveryItemView;
import com.visualspider.admin.ListDiscoveryPageView;
import com.visualspider.persistence.CrawlRule;
import com.visualspider.persistence.CrawlRuleMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatchDetailPreviewService {

    private final ListDiscoveryService listDiscoveryService;
    private final RulePreviewService rulePreviewService;
    private final CrawlRuleMapper crawlRuleMapper;

    public BatchDetailPreviewService(ListDiscoveryService listDiscoveryService,
                                     RulePreviewService rulePreviewService,
                                     CrawlRuleMapper crawlRuleMapper) {
        this.listDiscoveryService = listDiscoveryService;
        this.rulePreviewService = rulePreviewService;
        this.crawlRuleMapper = crawlRuleMapper;
    }

    public BatchDetailPreviewPageView preview(Long previewSessionId, Long listRuleId, Long detailRuleId) {
        ListDiscoveryPageView discoveryPage = listDiscoveryService.preview(previewSessionId, listRuleId);
        CrawlRule detailRule = requireRule(detailRuleId);

        List<BatchDetailPreviewItemView> items = new ArrayList<>();
        for (ListDiscoveryItemView discoveredItem : discoveryPage.items()) {
            try {
                var execution = rulePreviewService.previewBySourceUrl(detailRuleId, discoveredItem.detailUrl());
                items.add(new BatchDetailPreviewItemView(
                        discoveredItem.itemIndex(),
                        discoveredItem.titleText(),
                        discoveredItem.detailUrl(),
                        discoveredItem.timeText(),
                        true,
                        null,
                        execution.fieldResults()
                ));
            } catch (IllegalArgumentException | IllegalStateException ex) {
                items.add(new BatchDetailPreviewItemView(
                        discoveredItem.itemIndex(),
                        discoveredItem.titleText(),
                        discoveredItem.detailUrl(),
                        discoveredItem.timeText(),
                        false,
                        ex.getMessage(),
                        List.of()
                ));
            }
        }

        return new BatchDetailPreviewPageView(
                previewSessionId,
                listRuleId,
                detailRuleId,
                discoveryPage.ruleName(),
                detailRule.getRuleName(),
                discoveryPage.sourceUrl(),
                items
        );
    }

    private CrawlRule requireRule(Long ruleId) {
        CrawlRule rule = crawlRuleMapper.findById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Detail rule does not exist");
        }
        return rule;
    }
}
