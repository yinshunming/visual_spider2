package com.visualspider.scheduler;

import jakarta.annotation.PostConstruct;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class QuartzBootstrapScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuartzBootstrapScheduler.class);

    private final Scheduler scheduler;

    public QuartzBootstrapScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void scheduleBootstrapJob() throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(BootstrapLoggingJob.class)
                .withIdentity("bootstrapLoggingJob")
                .storeDurably(false)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("bootstrapLoggingTrigger")
                .forJob(jobDetail)
                .startAt(Date.from(Instant.now().plusSeconds(3)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                .build();

        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Bootstrap Quartz job scheduled.");
        }
    }
}

