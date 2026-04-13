package com.visualspider.scheduler;

import com.visualspider.persistence.CrawlRunLog;
import com.visualspider.persistence.CrawlRunLogMapper;
import com.visualspider.persistence.CrawlSnapshot;
import com.visualspider.persistence.CrawlSnapshotMapper;
import com.visualspider.persistence.CrawlTask;
import com.visualspider.persistence.CrawlTaskMapper;
import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrawlTaskService {

    private final CrawlTaskMapper crawlTaskMapper;
    private final CrawlRuleVersionMapper crawlRuleVersionMapper;
    private final CrawlRunLogMapper crawlRunLogMapper;
    private final CrawlSnapshotMapper crawlSnapshotMapper;
    private final Scheduler scheduler;

    public CrawlTaskService(CrawlTaskMapper crawlTaskMapper,
                            CrawlRuleVersionMapper crawlRuleVersionMapper,
                            CrawlRunLogMapper crawlRunLogMapper,
                            CrawlSnapshotMapper crawlSnapshotMapper,
                            Scheduler scheduler) {
        this.crawlTaskMapper = crawlTaskMapper;
        this.crawlRuleVersionMapper = crawlRuleVersionMapper;
        this.crawlRunLogMapper = crawlRunLogMapper;
        this.crawlSnapshotMapper = crawlSnapshotMapper;
        this.scheduler = scheduler;
    }

    public List<CrawlTask> findAllTasks() {
        return crawlTaskMapper.findAll();
    }

    public CrawlTask findTask(Long id) {
        return crawlTaskMapper.findById(id);
    }

    public List<CrawlRunLog> findRuns(Long taskId) {
        return crawlRunLogMapper.findByTaskId(taskId);
    }

    public CrawlRunLog findRun(Long runId) {
        return crawlRunLogMapper.findById(runId);
    }

    public List<CrawlSnapshot> findSnapshots(Long runId) {
        return crawlSnapshotMapper.findByRunLogId(runId);
    }

    @Transactional
    public Long saveTask(CrawlTask task) {
        validatePublishedVersion(task.getRuleVersionId());
        if (task.getId() == null) {
            crawlTaskMapper.insert(task);
        } else {
            crawlTaskMapper.update(task);
        }
        syncScheduler(task);
        return task.getId();
    }

    @Transactional
    public void pauseTask(Long taskId) {
        CrawlTask task = requireTask(taskId);
        task.setStatus("PAUSED");
        crawlTaskMapper.update(task);
        pauseScheduler(taskId);
    }

    @Transactional
    public void activateTask(Long taskId) {
        CrawlTask task = requireTask(taskId);
        validatePublishedVersion(task.getRuleVersionId());
        task.setStatus("ACTIVE");
        crawlTaskMapper.update(task);
        syncScheduler(task);
    }

    private void validatePublishedVersion(Long ruleVersionId) {
        CrawlRuleVersion version = crawlRuleVersionMapper.findById(ruleVersionId);
        if (version == null || !"PUBLISHED".equalsIgnoreCase(version.getStatus())) {
            throw new IllegalStateException("任务只能绑定已发布版本");
        }
    }

    private CrawlTask requireTask(Long taskId) {
        CrawlTask task = crawlTaskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        return task;
    }

    private void syncScheduler(CrawlTask task) {
        try {
            JobKey jobKey = jobKey(task.getId());
            TriggerKey triggerKey = triggerKey(task.getId());

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            if (!"ACTIVE".equalsIgnoreCase(task.getStatus())) {
                return;
            }

            JobDetail jobDetail = JobBuilder.newJob(CrawlTaskJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("taskId", task.getId())
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception ex) {
            throw new IllegalStateException("同步 Quartz 任务失败: " + ex.getMessage(), ex);
        }
    }

    private void pauseScheduler(Long taskId) {
        try {
            JobKey jobKey = jobKey(taskId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("暂停 Quartz 任务失败: " + ex.getMessage(), ex);
        }
    }

    private JobKey jobKey(Long taskId) {
        return new JobKey("crawlTaskJob-" + taskId);
    }

    private TriggerKey triggerKey(Long taskId) {
        return new TriggerKey("crawlTaskTrigger-" + taskId);
    }
}

