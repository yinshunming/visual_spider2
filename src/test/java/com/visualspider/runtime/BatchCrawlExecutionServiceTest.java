package com.visualspider.runtime;

import com.visualspider.admin.ListDiscoveryItemView;
import com.visualspider.persistence.CrawlTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class BatchCrawlExecutionServiceTest {

    private final ListDiscoveryService listDiscoveryService = mock(ListDiscoveryService.class);
    private final ArticleIngestionService articleIngestionService = mock(ArticleIngestionService.class);

    private final BatchCrawlExecutionService service = new BatchCrawlExecutionService(
            listDiscoveryService,
            articleIngestionService
    );

    @Test
    void shouldAggregateBatchExecutionStats() {
        CrawlTask task = new CrawlTask();
        task.setUrlTemplate("https://sports.sina.com.cn/nba/");
        task.setListRuleVersionId(21L);
        task.setRuleVersionId(31L);

        given(listDiscoveryService.discoverByPublishedVersion(eq(21L), eq("https://sports.sina.com.cn/nba/")))
                .willReturn(List.of(
                        new ListDiscoveryItemView(0, "News 1", "https://sports.sina.com.cn/nba/doc-1.shtml", "2026-04-16"),
                        new ListDiscoveryItemView(1, "News 2", "https://sports.sina.com.cn/nba/doc-2.shtml", "2026-04-16")
                ));
        given(articleIngestionService.ingestArticleByPublishedVersion(eq(31L), eq("https://sports.sina.com.cn/nba/doc-1.shtml")))
                .willReturn(ArticleIngestionResult.inserted("https://sports.sina.com.cn/nba/doc-1.shtml", "inserted"));
        given(articleIngestionService.ingestArticleByPublishedVersion(eq(31L), eq("https://sports.sina.com.cn/nba/doc-2.shtml")))
                .willReturn(ArticleIngestionResult.updated("https://sports.sina.com.cn/nba/doc-2.shtml", "updated"));

        CrawlExecutionSummary summary = service.execute(task);

        assertEquals("LIST_BATCH", summary.mode());
        assertEquals(2, summary.listDiscoveryCount());
        assertEquals(2, summary.detailCount());
        assertEquals(1, summary.articleInsertedCount());
        assertEquals(1, summary.articleUpdatedCount());
        assertEquals(0, summary.articleSkippedCount());
        assertEquals(0, summary.failureCount());
    }

    @Test
    void shouldCollectFailureReasonsDuringBatchExecution() {
        CrawlTask task = new CrawlTask();
        task.setUrlTemplate("https://sports.sina.com.cn/nba/");
        task.setListRuleVersionId(21L);
        task.setRuleVersionId(31L);

        given(listDiscoveryService.discoverByPublishedVersion(eq(21L), eq("https://sports.sina.com.cn/nba/")))
                .willReturn(List.of(
                        new ListDiscoveryItemView(0, "Broken", "https://sports.sina.com.cn/nba/doc-bad.shtml", "2026-04-16")
                ));
        given(articleIngestionService.ingestArticleByPublishedVersion(eq(31L), eq("https://sports.sina.com.cn/nba/doc-bad.shtml")))
                .willThrow(new IllegalStateException("title field missing"));

        CrawlExecutionSummary summary = service.execute(task);

        assertEquals(1, summary.failureCount());
        assertEquals(1, summary.failureReasons().size());
        assertEquals("https://sports.sina.com.cn/nba/doc-bad.shtml | title field missing", summary.failureReasons().getFirst());
    }
}
