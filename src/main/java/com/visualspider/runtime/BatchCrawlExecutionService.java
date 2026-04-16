package com.visualspider.runtime;

import com.visualspider.admin.ListDiscoveryItemView;
import com.visualspider.persistence.CrawlTask;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatchCrawlExecutionService {

    private final ListDiscoveryService listDiscoveryService;
    private final ArticleIngestionService articleIngestionService;

    public BatchCrawlExecutionService(ListDiscoveryService listDiscoveryService,
                                      ArticleIngestionService articleIngestionService) {
        this.listDiscoveryService = listDiscoveryService;
        this.articleIngestionService = articleIngestionService;
    }

    public CrawlExecutionSummary execute(CrawlTask task) {
        if (task.getListRuleVersionId() == null) {
            ArticleIngestionResult result = articleIngestionService.ingestArticleByPublishedVersion(task.getRuleVersionId(), task.getUrlTemplate());
            return new CrawlExecutionSummary(
                    "SINGLE_DETAIL",
                    0,
                    1,
                    result.action() == ArticleIngestionAction.INSERTED ? 1 : 0,
                    result.action() == ArticleIngestionAction.UPDATED ? 1 : 0,
                    result.action() == ArticleIngestionAction.SKIPPED ? 1 : 0,
                    0,
                    List.of()
            );
        }

        List<ListDiscoveryItemView> items = listDiscoveryService.discoverByPublishedVersion(task.getListRuleVersionId(), task.getUrlTemplate());
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        List<String> failureReasons = new ArrayList<>();
        for (ListDiscoveryItemView item : items) {
            try {
                ArticleIngestionResult result = articleIngestionService.ingestArticleByPublishedVersion(task.getRuleVersionId(), item.detailUrl());
                switch (result.action()) {
                    case INSERTED -> inserted++;
                    case UPDATED -> updated++;
                    case SKIPPED -> skipped++;
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                failureReasons.add(item.detailUrl() + " | " + ex.getMessage());
            }
        }

        return new CrawlExecutionSummary(
                "LIST_BATCH",
                items.size(),
                items.size(),
                inserted,
                updated,
                skipped,
                failureReasons.size(),
                failureReasons
        );
    }
}
