package com.visualspider.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BootstrapLoggingJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(BootstrapLoggingJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Bootstrap Quartz job executed successfully.");
    }
}

