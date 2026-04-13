package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CrawlRunLogMapper {

    @Insert("""
            insert into crawl_run_log (task_id, rule_version_id, status, source_url, duration_ms, error_message, started_at, finished_at)
            values (#{taskId}, #{ruleVersionId}, #{status}, #{sourceUrl}, #{durationMs}, #{errorMessage}, #{startedAt}, #{finishedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrawlRunLog runLog);

    @Update("""
            update crawl_run_log
            set status = #{status},
                source_url = #{sourceUrl},
                duration_ms = #{durationMs},
                error_message = #{errorMessage},
                started_at = #{startedAt},
                finished_at = #{finishedAt}
            where id = #{id}
            """)
    int update(CrawlRunLog runLog);

    @Select("""
            select id, task_id, rule_version_id, status, source_url, duration_ms, error_message, started_at, finished_at, created_at
            from crawl_run_log
            where id = #{id}
            """)
    @Results(id = "crawlRunLogResult", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "ruleVersionId", column = "rule_version_id"),
            @Result(property = "sourceUrl", column = "source_url"),
            @Result(property = "durationMs", column = "duration_ms"),
            @Result(property = "errorMessage", column = "error_message"),
            @Result(property = "startedAt", column = "started_at"),
            @Result(property = "finishedAt", column = "finished_at"),
            @Result(property = "createdAt", column = "created_at")
    })
    CrawlRunLog findById(Long id);

    @Select("""
            select id, task_id, rule_version_id, status, source_url, duration_ms, error_message, started_at, finished_at, created_at
            from crawl_run_log
            where task_id = #{taskId}
            order by id desc
            """)
    @Results(id = "crawlRunLogListResult", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "ruleVersionId", column = "rule_version_id"),
            @Result(property = "sourceUrl", column = "source_url"),
            @Result(property = "durationMs", column = "duration_ms"),
            @Result(property = "errorMessage", column = "error_message"),
            @Result(property = "startedAt", column = "started_at"),
            @Result(property = "finishedAt", column = "finished_at"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CrawlRunLog> findByTaskId(Long taskId);
}

