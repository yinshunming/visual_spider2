package com.visualspider.scheduler;

import com.visualspider.persistence.CrawlRunLog;
import com.visualspider.persistence.CrawlRunLogMapper;
import com.visualspider.persistence.CrawlTask;
import com.visualspider.persistence.CrawlTaskMapper;
import com.visualspider.runtime.BatchCrawlExecutionService;
import com.visualspider.runtime.CrawlExecutionSummary;
import com.visualspider.runtime.PageRuntimeSnapshot;
import com.visualspider.runtime.PlaywrightService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class CrawlTaskJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskJob.class);

    private final CrawlTaskMapper crawlTaskMapper;
    private final CrawlRunLogMapper crawlRunLogMapper;
    private final BatchCrawlExecutionService batchCrawlExecutionService;
    private final TaskSnapshotService taskSnapshotService;
    private final PlaywrightService playwrightService;

    public CrawlTaskJob(CrawlTaskMapper crawlTaskMapper,
                        CrawlRunLogMapper crawlRunLogMapper,
                        BatchCrawlExecutionService batchCrawlExecutionService,
                        TaskSnapshotService taskSnapshotService,
                        PlaywrightService playwrightService) {
        this.crawlTaskMapper = crawlTaskMapper;
        this.crawlRunLogMapper = crawlRunLogMapper;
        this.batchCrawlExecutionService = batchCrawlExecutionService;
        this.taskSnapshotService = taskSnapshotService;
        this.playwrightService = playwrightService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Long taskId = context.getMergedJobDataMap().getLong("taskId");
        CrawlTask task = crawlTaskMapper.findById(taskId);
        if (task == null || !"ACTIVE".equalsIgnoreCase(task.getStatus())) {
            return;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        CrawlRunLog runLog = new CrawlRunLog();
        runLog.setTaskId(task.getId());
        runLog.setRuleVersionId(task.getRuleVersionId());
        runLog.setStatus("RUNNING");
        runLog.setSourceUrl(task.getUrlTemplate());
        runLog.setStartedAt(startedAt);
        crawlRunLogMapper.insert(runLog);

        try {
            PageRuntimeSnapshot runtimeSnapshot = playwrightService.captureRuntimeSnapshot(task.getUrlTemplate());
            CrawlExecutionSummary summary = batchCrawlExecutionService.execute(task);
            LocalDateTime finishedAt = LocalDateTime.now();
            runLog.setStatus("SUCCESS");
            runLog.setFinishedAt(finishedAt);
            runLog.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
            runLog.setErrorMessage(null);
            crawlRunLogMapper.update(runLog);

            taskSnapshotService.writeSnapshot(runLog, "extract-result", toSummaryJson(summary), "json");
            taskSnapshotService.writeSnapshot(runLog, "page-html", runtimeSnapshot.htmlContent(), "html");
            taskSnapshotService.copySnapshotFile(runLog, "page-png", runtimeSnapshot.screenshotPath(), "png");
            log.info("Crawl task {} executed successfully.", taskId);
        } catch (Exception ex) {
            LocalDateTime finishedAt = LocalDateTime.now();
            runLog.setStatus("FAILED");
            runLog.setFinishedAt(finishedAt);
            runLog.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
            runLog.setErrorMessage(ex.getMessage());
            crawlRunLogMapper.update(runLog);
            taskSnapshotService.writeSnapshot(runLog, "extract-result", "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}", "json");
            log.error("Crawl task {} failed: {}", taskId, ex.getMessage());
        }
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String toSummaryJson(CrawlExecutionSummary summary) {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"mode\":\"").append(escapeJson(summary.mode())).append("\",")
                .append("\"listDiscoveryCount\":").append(summary.listDiscoveryCount()).append(",")
                .append("\"detailCount\":").append(summary.detailCount()).append(",")
                .append("\"articleInsertedCount\":").append(summary.articleInsertedCount()).append(",")
                .append("\"articleUpdatedCount\":").append(summary.articleUpdatedCount()).append(",")
                .append("\"articleSkippedCount\":").append(summary.articleSkippedCount()).append(",")
                .append("\"failureCount\":").append(summary.failureCount()).append(",")
                .append("\"failureReasons\":[");
        for (int i = 0; i < summary.failureReasons().size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("\"").append(escapeJson(summary.failureReasons().get(i))).append("\"");
        }
        builder.append("]}");
        return builder.toString();
    }
}
