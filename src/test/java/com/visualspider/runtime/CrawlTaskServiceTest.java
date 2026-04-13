package com.visualspider.runtime;

import com.visualspider.persistence.CrawlRuleVersion;
import com.visualspider.persistence.CrawlRuleVersionMapper;
import com.visualspider.persistence.CrawlRunLogMapper;
import com.visualspider.persistence.CrawlSnapshotMapper;
import com.visualspider.persistence.CrawlTask;
import com.visualspider.persistence.CrawlTaskMapper;
import com.visualspider.scheduler.CrawlTaskService;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class CrawlTaskServiceTest {

    private final CrawlTaskMapper crawlTaskMapper = mock(CrawlTaskMapper.class);
    private final CrawlRuleVersionMapper crawlRuleVersionMapper = mock(CrawlRuleVersionMapper.class);
    private final CrawlRunLogMapper crawlRunLogMapper = mock(CrawlRunLogMapper.class);
    private final CrawlSnapshotMapper crawlSnapshotMapper = mock(CrawlSnapshotMapper.class);
    private final Scheduler scheduler = mock(Scheduler.class);

    private final CrawlTaskService service = new CrawlTaskService(
            crawlTaskMapper,
            crawlRuleVersionMapper,
            crawlRunLogMapper,
            crawlSnapshotMapper,
            scheduler
    );

    @Test
    void shouldSaveActiveTaskWhenVersionPublished() throws Exception {
        CrawlRuleVersion version = new CrawlRuleVersion();
        version.setId(11L);
        version.setStatus("PUBLISHED");

        CrawlTask task = new CrawlTask();
        task.setId(5L);
        task.setTaskName("demo");
        task.setUrlTemplate("https://www.sina.com.cn");
        task.setRuleVersionId(11L);
        task.setCronExpression("0 0/5 * * * ?");
        task.setStatus("ACTIVE");

        given(crawlRuleVersionMapper.findById(11L)).willReturn(version);
        given(scheduler.checkExists(any(JobKey.class))).willReturn(false);

        Long id = service.saveTask(task);

        assertEquals(5L, id);
        then(crawlTaskMapper).should().update(task);
    }
}
